#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@File    ：my_fcn.py
@Author  ：Zhang Qihang
@Date    ：2022/4/18 14:18 
@Description :
"""

import keras
import numpy as np
import sklearn
from utils.utils import save_logs


class Classifier_FCN:

    def __init__(self, output_directory, input_shape, nb_classes, nb_prototypes, classes,
                 verbose=False, load_init_weights=False):
        self.output_directory = output_directory
        self.model = self.build_model(input_shape, nb_classes)
        self.nb_prototypes = nb_prototypes
        self.classes = classes
        if (verbose == True):
            # verbose为1，表示显示信息
            self.model.summary()
        self.verbose = verbose
        if load_init_weights == True:
            self.model.load_weights(self.output_directory.
                                    replace('resnet_augment', 'resnet')
                                    + '/fcn_model_init.hdf5')
        else:
            # this is without data augmentation so we should save inital non trained weights
            # to be used later as initialization and train the model with data augmentaiton
            self.model.save_weights(self.output_directory + 'fcn_model_init.hdf5')

    def build_model(self, input_shape, nb_classes):
        n_feature_maps = 128

        input_layer = keras.layers.Input(input_shape)
        # dropout
        # dropout_layer = keras.layers.Dropout(0.2)(input_layer)
        conv1 = keras.layers.Conv1D(n_feature_maps, 8, 1, padding='same')(input_layer)
        conv1 = keras.layers.BatchNormalization()(conv1)
        conv1 = keras.layers.Activation('relu')(conv1)

        # dropout
        # dropout_layer = keras.layers.Dropout(0.2)(conv1)
        conv2 = keras.layers.Conv1D(n_feature_maps * 2, 5, 1, padding='same')(conv1)
        conv2 = keras.layers.BatchNormalization()(conv2)
        conv2 = keras.layers.Activation('relu')(conv2)

        # dropout
        # dropout_layer = keras.layers.Dropout(0.2)(conv2)
        conv3 = keras.layers.Conv1D(n_feature_maps, 3, 1, padding='same')(conv2)
        conv3 = keras.layers.BatchNormalization()(conv3)
        conv3 = keras.layers.Activation('relu')(conv3)

        # FINAL, 全连接层
        full = keras.layers.GlobalAveragePooling1D()(conv3)
        # 添加dropout
        full = keras.layers.Dropout(0.2)(full)
        output_layer = keras.layers.Dense(nb_classes, activation='softmax')(full)

        model = keras.models.Model(inputs=input_layer, outputs=output_layer)

        model.compile(loss='categorical_crossentropy', optimizer=keras.optimizers.Adam(),
                      metrics=['accuracy'])

        reduce_lr = keras.callbacks.ReduceLROnPlateau(monitor='loss', factor=0.5, patience=20, min_lr=0.0001)
        early_stopping = keras.callbacks.EarlyStopping(monitor='loss', patience=20, mode='auto')

        file_path = self.output_directory + 'best_fcn_model.hdf5'

        model_checkpoint = keras.callbacks.ModelCheckpoint(filepath=file_path, monitor='val_accuracy',
                                                           save_best_only=True, mode='auto')

        self.callbacks = [reduce_lr, model_checkpoint]

        return model

    def fit(self, x_train, y_train, x_test, y_true):
        # convert to binary
        # transform the labels from integers to one hot vectors
        # 转换标签为独热码的形式
        self.enc = sklearn.preprocessing.OneHotEncoder()
        self.enc.fit(np.concatenate((y_train, y_true), axis=0).reshape(-1, 1))
        y_train_int = y_train
        y_train = self.enc.transform(y_train.reshape(-1, 1)).toarray()
        y_test = self.enc.transform(y_true.reshape(-1, 1)).toarray()

        # x_val and y_val are only used to monitor the test loss and NOT for training
        batch_size = 16

        nb_epochs = 1000

        mini_batch_size = int(min(x_train.shape[0] / 10, batch_size))

        if len(x_train) > 4000:  # for ElectricDevices
            mini_batch_size = 128
        x_train = x_train.astype('float64')
        x_test = x_test.astype('float64')
        hist = self.model.fit(x_train, y_train, batch_size=mini_batch_size, epochs=nb_epochs,
                              verbose=self.verbose, validation_data=(x_test, y_test), callbacks=self.callbacks)

        model = keras.models.load_model(self.output_directory + 'best_fcn_model.hdf5')

        y_pred = model.predict(x_test)

        # convert the predicted from binary to integer
        y_pred = np.argmax(y_pred, axis=1)

        keras.backend.clear_session()

        save_logs(self.output_directory, hist, y_pred, y_true, 0.0)

        return y_pred
