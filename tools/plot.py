#!/usr/bin/env python
import glob
import numpy as np
import matplotlib
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
from matplotlib import gridspec
from enum import Enum

# Set timezone which will be used by x-axis.
matplotlib.rcParams['timezone'] = 'US/Pacific'
dayFmt=mdates.DateFormatter('%a-%b-%d')
hrFmt=mdates.DateFormatter('%H:00')

class DataSource(Enum):
    PIR = 1
    MIC_AMPLITUDE = 2
    ACCEL = 3
    GYRO = 4

enabledPlotDataSource = [DataSource.PIR, DataSource.MIC_AMPLITUDE, DataSource.ACCEL, DataSource.GYRO]

def getGridSpec():
    rows = 0
    for pds in enabledPlotDataSource:
        if pds == DataSource.PIR:
            rows += 1
        elif pds == DataSource.MIC_AMPLITUDE:
            rows += 1
        elif pds == DataSource.ACCEL:
            rows += 3
        elif pds == DataSource.GYRO:
            rows += 3
    return rows

currGridRow = 0

gs = gridspec.GridSpec(getGridSpec(), 1)

dataDir = "/tmp/sensor_data"
# dataDir = "./sensor_data"

dataFileList = glob.glob(dataDir + '/hc_sr_501_*')
data = []  # list of ndarray
for filePath in dataFileList:
  print filePath
  data.append(np.genfromtxt(filePath, delimiter="\n").flatten())
# flatten the list of ndarray to 1D ndarray
data = np.concatenate(data).ravel()
data = mdates.epoch2num(data/1000)

# Plot histogram.
ax0 = plt.subplot(gs[currGridRow])
currGridRow += 1
ax0.hist(data, bins=1400, color='lightblue')

# Set x-axis format.
ax0.xaxis.set_major_locator(mdates.DayLocator())
ax0.xaxis.set_major_formatter(dayFmt)
ax0.xaxis.set_minor_locator(mdates.HourLocator(byhour=[2,4,6,8,10,12,14,16,18,20,22,24]))
ax0.xaxis.set_minor_formatter(hrFmt)



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
ax1 = plt.subplot(gs[currGridRow], sharex = ax0)
currGridRow += 1
ax1.plot(x, y)
# remove last tick label for the second subplot
yticks = ax1.yaxis.get_major_ticks()
yticks[-1].label1.set_visible(False)



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
ax1 = plt.subplot(gs[currGridRow], sharex = ax0)
currGridRow += 1
ax1.plot(x, y)
# remove last tick label for the second subplot
yticks = ax1.yaxis.get_major_ticks()
yticks[-1].label1.set_visible(False)

y = map(lambda x: x[2], accel_data)
ax2 = plt.subplot(gs[currGridRow], sharex = ax0)
currGridRow += 1
ax2.plot(x, y)
# remove last tick label for the second subplot
yticks = ax2.yaxis.get_major_ticks()
yticks[-1].label1.set_visible(False)

y = map(lambda x: x[3], accel_data)
ax3 = plt.subplot(gs[currGridRow], sharex = ax0)
currGridRow += 1
ax3.plot(x, y)
# remove last tick label for the second subplot
yticks = ax3.yaxis.get_major_ticks()
yticks[-1].label1.set_visible(False)



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
ax4 = plt.subplot(gs[currGridRow], sharex = ax0)
currGridRow += 1
ax4.plot(x, y)
# remove last tick label for the second subplot
yticks = ax4.yaxis.get_major_ticks()
yticks[-1].label1.set_visible(False)

y = map(lambda x: x[2], gyro_data)
ax5 = plt.subplot(gs[currGridRow], sharex = ax0)
currGridRow += 1
ax5.plot(x, y)
# remove last tick label for the second subplot
yticks = ax5.yaxis.get_major_ticks()
yticks[-1].label1.set_visible(False)

y = map(lambda x: x[3], gyro_data)
ax6 = plt.subplot(gs[currGridRow], sharex = ax0)
currGridRow += 1
ax6.plot(x, y)
# remove last tick label for the second subplot
yticks = ax6.yaxis.get_major_ticks()
yticks[-1].label1.set_visible(False)




plt.setp(ax0.get_xticklabels(), visible=False)


# remove vertical gap between subplots
plt.subplots_adjust(hspace=.0)

plt.show()
