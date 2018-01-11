#!/usr/bin/env python
import glob
import numpy as np
import matplotlib
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
import matplotlib.dates as mdates

# Set timezone which will be used by x-axis.
matplotlib.rcParams['timezone'] = 'US/Pacific'

dataDir = "/tmp/sensor_data"
dataFileList = glob.glob(dataDir + '/*')
data = []  # list of ndarray
print type(data)
for filePath in dataFileList:
  print filePath
  data.append(np.genfromtxt(filePath, delimiter="\n").flatten())
# flatten the list of ndarray to 1D ndarray
data = np.concatenate(data).ravel()
data = mdates.epoch2num(data/1000)

# Plot histogram.
fig, ax = plt.subplots(1,1)
ax.hist(data, bins=1400, color='lightblue')

# Set x-axis format.
dayFmt=mdates.DateFormatter('%a-%b-%d')
hrFmt=mdates.DateFormatter('%H:00')
ax.xaxis.set_major_locator(mdates.DayLocator())
ax.xaxis.set_major_formatter(dayFmt)
ax.xaxis.set_minor_locator(mdates.HourLocator(byhour=[2,4,6,8,10,12,14,16,18,20,22,24]))
ax.xaxis.set_minor_formatter(hrFmt)

plt.show()
