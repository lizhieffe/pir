#!/usr/bin/env python
import glob
import numpy as np
import matplotlib
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
import sys
from matplotlib import gridspec
from enum import Enum

class DataSource(Enum):
    PIR = 1
    MIC_AMPLITUDE = 2
    ACCEL = 3
    GYRO = 4

def getPlotRow(dataSource):
    if dataSource == DataSource.PIR:
        return 1
    elif dataSource == DataSource.MIC_AMPLITUDE:
        return 1
    elif dataSource == DataSource.ACCEL:
        return 3
    elif dataSource == DataSource.GYRO:
        return 3
    else:
        assert False, "ERROR: Plot row is not defined"

enabledPlotDataSource = [
                         DataSource.PIR,
                         DataSource.MIC_AMPLITUDE,
                         DataSource.ACCEL,
                         DataSource.GYRO,
                        ]

def getGridSpec():
    rows = 0
    for pds in enabledPlotDataSource:
        rows += getPlotRow(pds)
    return rows

def addPirPlot(dataDir, gridSpec, currGridRow, sharedXAxis):
    print "Start ploting PIR data"

    dataFileList = glob.glob(dataDir + '/hc_sr_501_*')
    data = []  # list of ndarray
    for filePath in dataFileList:
      print filePath
      data.append(np.genfromtxt(filePath, delimiter="\n").flatten())
    # flatten the list of ndarray to 1D ndarray
    data = np.concatenate(data).ravel()
    data = mdates.epoch2num(data/1000)
    
    # Plot histogram.
    ax = plt.subplot(gridSpec[currGridRow], sharex = sharedXAxis)
    ax.hist(data, bins=1400, color='lightblue')
    
    print "PIR plot is done."
    return [ax]

def addMicAmplitudePlot(dataDir, gridSpec, currGridRow, sharedXAxis):
    print "Start ploting MIC amplitude data"
    
    dataFileList = glob.glob(dataDir + '/mic_amplitude_*')
    amplitude_data = []  # list of ndarray
    for filePath in dataFileList:
      print filePath
      with open(filePath) as f:
          for line in f:
              nums = [float(x) for x in line.split(" ")]
              amplitude_data.append(nums)
    
    x = map(lambda x: mdates.epoch2num(int(x[0] / 1000)), amplitude_data)
    y = map(lambda x: x[1], amplitude_data)
    ax = plt.subplot(gridSpec[currGridRow], sharex = sharedXAxis)
    ax.plot(x, y)
    # remove last tick label for the second subplot
    yticks = ax.yaxis.get_major_ticks()
    yticks[-1].label1.set_visible(False)

    print "MIC Amplitude plot is done."
    return [ax]

def addAccelPlot(dataDir, gridSpec, currGridRow, sharedXAxis):
    print "Start ploting ACCEL data"

    dataFileList = glob.glob(dataDir + '/mpu_6500_accel_*')
    accel_data = []  # list of ndarray
    for filePath in dataFileList:
      print filePath
      with open(filePath) as f:
          for line in f:
              nums = [float(x) for x in line.split(" ")]
              accel_data.append(nums)
    
    x = map(lambda x: mdates.epoch2num(int(x[0] / 1000)), accel_data)
    y = map(lambda x: x[1], accel_data)
    ax1 = plt.subplot(gridSpec[currGridRow], sharex = sharedXAxis)
    ax1.plot(x, y)
    # remove last tick label for the second subplot
    yticks = ax1.yaxis.get_major_ticks()
    yticks[-1].label1.set_visible(False)
    
    y = map(lambda x: x[2], accel_data)
    ax2 = plt.subplot(gridSpec[currGridRow + 1], sharex = ax1)
    ax2.plot(x, y)
    # remove last tick label for the second subplot
    yticks = ax2.yaxis.get_major_ticks()
    yticks[-1].label1.set_visible(False)
    
    y = map(lambda x: x[3], accel_data)
    ax3 = plt.subplot(gridSpec[currGridRow + 2], sharex = ax2)
    ax3.plot(x, y)
    # remove last tick label for the second subplot
    yticks = ax3.yaxis.get_major_ticks()
    yticks[-1].label1.set_visible(False)

    print "ACCEL plot is done."
    return [ax1, ax2, ax3]

def addGyroPlot(dataDir, gridSpec, currGridRow, sharedXAxis):
    print "Start ploting GYRO data"

    dataFileList = glob.glob(dataDir + '/mpu_6500_gyro_*')
    gyro_data = []  # list of ndarray
    for filePath in dataFileList:
      print filePath
      with open(filePath) as f:
          for line in f:
              nums = [float(x) for x in line.split(" ")]
              gyro_data.append(nums)
    
    x = map(lambda x: mdates.epoch2num(int(x[0] / 1000)), gyro_data)
    y = map(lambda x: x[1], gyro_data)
    ax4 = plt.subplot(gridSpec[currGridRow], sharex = sharedXAxis)
    ax4.plot(x, y)
    # remove last tick label for the second subplot
    yticks = ax4.yaxis.get_major_ticks()
    yticks[-1].label1.set_visible(False)
    
    y = map(lambda x: x[2], gyro_data)
    ax5 = plt.subplot(gridSpec[currGridRow + 1], sharex = ax4)
    ax5.plot(x, y)
    # remove last tick label for the second subplot
    yticks = ax5.yaxis.get_major_ticks()
    yticks[-1].label1.set_visible(False)
    
    y = map(lambda x: x[3], gyro_data)
    ax6 = plt.subplot(gridSpec[currGridRow + 2], sharex = ax5)
    ax6.plot(x, y)
    # remove last tick label for the second subplot
    yticks = ax6.yaxis.get_major_ticks()
    yticks[-1].label1.set_visible(False)

    print "GYRO plot is done."
    return [ax4, ax5, ax6]

def main(argv):
    currGridRow = 0
    gs = gridspec.GridSpec(getGridSpec(), 1)
    
    dataDir = "/tmp/sensor_data"
    # dataDir = "./sensor_data"

    # Set timezone which will be used by x-axis.
    matplotlib.rcParams['timezone'] = 'US/Pacific'
    dayFmt=mdates.DateFormatter('%a-%b-%d')
    hrFmt=mdates.DateFormatter('%H:00')
    
    ax0 = plt.axes()
    
    for pds in enabledPlotDataSource:
        plotted = False
        if pds == DataSource.PIR:
            localAx = addPirPlot(dataDir, gs, currGridRow, ax0)
            plotted = True
        elif pds == DataSource.MIC_AMPLITUDE:
            localAx = addMicAmplitudePlot(dataDir, gs, currGridRow, ax0)
            plotted = True
        elif pds == DataSource.ACCEL:
            localAx = addAccelPlot(dataDir, gs, currGridRow, ax0)
            plotted = True
        elif pds == DataSource.GYRO:
            localAx = addGyroPlot(dataDir, gs, currGridRow, ax0)
            plotted = True
    
        if plotted == True:
            if currGridRow == 0:
                ax0 = localAx[0]
    
                # Set x-axis format.
                ax0.xaxis.set_major_locator(mdates.DayLocator())
                ax0.xaxis.set_major_formatter(dayFmt)
                ax0.xaxis.set_minor_locator(mdates.HourLocator(byhour=[2,4,6,8,10,12,14,16,18,20,22,24]))
                ax0.xaxis.set_minor_formatter(hrFmt)
    
            currGridRow += getPlotRow(pds)
    
            i = 0
            for lax in localAx:
                # remove last tick label for the second subplot
                yticks = lax.yaxis.get_major_ticks()
                yticks[-1].label1.set_visible(False)
    
                # TODO: remove the redundant x ticks.
                # plt.setp(lax.get_xticklabels(), visible=False)
    
    # remove vertical gap between subplots
    plt.subplots_adjust(hspace=.0)
    
    plt.show()

if __name__ == "__main__":
    main(sys.argv)
