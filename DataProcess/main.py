# This is a sample Python script.

# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.
import data_process
from config import *
import glob
import os

# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    rp_path = glob.glob(os.path.join(READ_PRINT_FILES_PATH, '*.csv'))
    hop_path = glob.glob(os.path.join(HOP_FILES_PATH, '*.csv'))
    # for path in total_path:
    #     data_process.rp_process(path)
    for path in hop_path:
        print(path)
        data_process.hop_process(path)
