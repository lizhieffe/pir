#!/usr/bin/env python
import glob
import numpy as np
import matplotlib
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
import matplotlib.dates as mdates

# Set timezone which will be used by x-axis.
matplotlib.rcParams['timezone'] = 'US/Pacific'
dayFmt=mdates.DateFormatter('%a-%b-%d')
hrFmt=mdates.DateFormatter('%H:00')

dataDir = "/tmp/sensor_data"
dataDir = "./sensor_data"
dataFileList = glob.glob(dataDir + '/hc_sr_501_*')
data = []  # list of ndarray
for filePath in dataFileList:
  print filePath
  data.append(np.genfromtxt(filePath, delimiter="\n").flatten())
# flatten the list of ndarray to 1D ndarray
data = np.concatenate(data).ravel()
data = mdates.epoch2num(data/1000)

# Plot histogram.
ax0 = plt.subplot(7,1,1)
ax0.hist(data, bins=1400, color='lightblue')

# Set x-axis format.
ax0.xaxis.set_major_locator(mdates.DayLocator())
ax0.xaxis.set_major_formatter(dayFmt)
ax0.xaxis.set_minor_locator(mdates.HourLocator(byhour=[2,4,6,8,10,12,14,16,18,20,22,24]))
ax0.xaxis.set_minor_formatter(hrFmt)





dataFileList = glob.glob(dataDir + '/mpu_6500_accel_*')
accel_data = []  # list of ndarray
for filePath in dataFileList:
  print filePath
  with open(filePath) as f:
      for line in f:
          nums = [float(x) for x in line.split(" ")]
          accel_data.append(nums)

x = map(lambda x: int(x[0] / 1000), accel_data)
y = map(lambda x: x[1], accel_data)
ax1 = plt.subplot(7, 1, 2, sharex = ax0)
ax1.plot(x, y)
# ax1.xaxis.set_major_locator(mdates.DayLocator())
# ax1.xaxis.set_major_formatter(dayFmt)
# ax1.xaxis.set_minor_locator(mdates.HourLocator(byhour=[2,4,6,8,10,12,14,16,18,20,22,24]))
# ax1.xaxis.set_minor_formatter(hrFmt)
# remove last tick label for the second subplot
yticks = ax1.yaxis.get_major_ticks()
yticks[-1].label1.set_visible(False)

y = map(lambda x: x[2], accel_data)
ax2 = plt.subplot(7, 1, 3, sharex = ax0)
ax2.plot(x, y)
# ax1.xaxis.set_major_locator(mdates.DayLocator())
# ax1.xaxis.set_major_formatter(dayFmt)
# ax1.xaxis.set_minor_locator(mdates.HourLocator(byhour=[2,4,6,8,10,12,14,16,18,20,22,24]))
# ax1.xaxis.set_minor_formatter(hrFmt)
# remove last tick label for the second subplot
yticks = ax2.yaxis.get_major_ticks()
yticks[-1].label1.set_visible(False)

y = map(lambda x: x[3], accel_data)
ax3 = plt.subplot(7, 1, 4, sharex = ax0)
ax3.plot(x, y)
# ax1.xaxis.set_major_locator(mdates.DayLocator())
# ax1.xaxis.set_major_formatter(dayFmt)
# ax1.xaxis.set_minor_locator(mdates.HourLocator(byhour=[2,4,6,8,10,12,14,16,18,20,22,24]))
# ax1.xaxis.set_minor_formatter(hrFmt)
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

x = map(lambda x: int(x[0] / 1000), gyro_data)
y = map(lambda x: x[1], gyro_data)
ax4 = plt.subplot(7, 1, 5, sharex = ax0)
ax4.plot(x, y)
# ax1.xaxis.set_major_locator(mdates.DayLocator())
# ax1.xaxis.set_major_formatter(dayFmt)
# ax1.xaxis.set_minor_locator(mdates.HourLocator(byhour=[2,4,6,8,10,12,14,16,18,20,22,24]))
# ax1.xaxis.set_minor_formatter(hrFmt)
# remove last tick label for the second subplot
yticks = ax4.yaxis.get_major_ticks()
yticks[-1].label1.set_visible(False)

y = map(lambda x: x[2], gyro_data)
ax5 = plt.subplot(7, 1, 6, sharex = ax0)
ax5.plot(x, y)
# ax1.xaxis.set_major_locator(mdates.DayLocator())
# ax1.xaxis.set_major_formatter(dayFmt)
# ax1.xaxis.set_minor_locator(mdates.HourLocator(byhour=[2,4,6,8,10,12,14,16,18,20,22,24]))
# ax1.xaxis.set_minor_formatter(hrFmt)
# remove last tick label for the second subplot
yticks = ax5.yaxis.get_major_ticks()
yticks[-1].label1.set_visible(False)

y = map(lambda x: x[3], gyro_data)
ax6 = plt.subplot(7, 1, 7, sharex = ax0)
ax6.plot(x, y)
# ax1.xaxis.set_major_locator(mdates.DayLocator())
# ax1.xaxis.set_major_formatter(dayFmt)
# ax1.xaxis.set_minor_locator(mdates.HourLocator(byhour=[2,4,6,8,10,12,14,16,18,20,22,24]))
# ax1.xaxis.set_minor_formatter(hrFmt)
# remove last tick label for the second subplot
yticks = ax6.yaxis.get_major_ticks()
yticks[-1].label1.set_visible(False)




# plt.setp(ax0.get_xticklabels(), visible=False)


# remove vertical gap between subplots
# plt.subplots_adjust(hspace=.0)

plt.show()
