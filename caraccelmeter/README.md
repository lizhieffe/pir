In case part of the screen is not filled by system, it is highly probably due to
the incorrect config of the boot file. To fix, mount the sd card with
Androidthings to your computer. Then open RPIBOOT/config.txt, and add the
following lines:

```
framebuffer_width=800
framebuffer_height=480
hdmi_force_hotplug=1
hdmi_group=2
hdmi_mode=87
hdmi_cvt 800 480 60 6 0 0 0
```
