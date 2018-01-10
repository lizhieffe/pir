#!/usr/bin/env python
import numpy as np
import matplotlib
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
import matplotlib.dates as mdates

# Set timezone which will be used by x-axis.
matplotlib.rcParams['timezone'] = 'US/Pacific'

data = np.genfromtxt("/tmp/sensor_data/2018-01-10",delimiter="\n")
# Data is unixtime in MS. Convert the epoch format to matplotlib date format 
data = mdates.epoch2num(data/1000)

# Plot histogram.
fig, ax = plt.subplots(1,1)
ax.hist(data, bins=100, color='lightblue')

# Set x-axis format.
dayFmt=mdates.DateFormatter('%a-%b-%d')
hrFmt=mdates.DateFormatter('%H:00')
ax.xaxis.set_major_locator(mdates.DayLocator())
ax.xaxis.set_major_formatter(dayFmt)
ax.xaxis.set_minor_locator(mdates.HourLocator(byhour=[2,4,6,8,10,12,14,16,18,20,22,24]))
ax.xaxis.set_minor_formatter(hrFmt)

plt.show()
