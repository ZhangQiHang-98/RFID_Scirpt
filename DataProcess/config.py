#!/usr/bin/env python
# -*- coding: UTF-8 -*-
"""
@Project ：DataProcess
@File    ：config.py
@Author  ：Zhang Qihang
@Date    ：2021/11/8 13:21
"""

READ_PRINT_FILES_PATH = "../data/read_print"
HOP_FILES_PATH = "../data/hop"
DELTA = 0
REFER_CHANNEL = 920.625
REFER_POWER = 20
HAMPEL = 10
ALPHA_PATH = "../data/my_paper/alpha"
TEST_PATH = "../data/my_paper/test"
PEN_PATH = "F:\\RFID_Scirpt\\data\\dataset\\origin"
AUTH_PATH = "F:\\RFID_Scirpt\\data\\init_data\\my_paper\\auth"
COMMON_COLUMNS = ["tag", "freq", "time", "phase", "rssi"]
MORE_COLUMNS = ["tag", "set_freq", "real_freq", "time", "rss", "phase", "set_power", "real_power"]


def get_power_list(start_power, end_power):
    power_list = []
    len = (end_power - start_power) / 2.5 + 1
    for i in range(int(len)):
        power_list.append(start_power + i * 2.5)
    return power_list


def get_freq_list(start_freq, end_freq):
    freq_list = []
    len = (end_freq - start_freq) / 0.5 + 1
    for i in range(int(len)):
        freq_list.append(start_freq + i * 0.5)
    return freq_list


def get_power_index(power):
    return int((power - REFER_POWER) / 2.5)


def get_freq_index(freq):
    return int((freq - REFER_CHANNEL) / 0.5)
