package com.zll.androidthings.drivers.tcs_34725;

/**
 * Created by lizhieffe on 1/21/18.
 */

public class Color {
    public final int red;
    public final int green;
    public final int blue;
    public final int clear;

    public Color(int red, int green, int blue, int clear) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.clear = clear;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[red]");
        sb.append(this.red);
        sb.append(" ");
        sb.append("[green]");
        sb.append(this.green);
        sb.append(" ");
        sb.append("[blue]");
        sb.append(this.blue);
        sb.append(" ");
        sb.append("[clear]");
        sb.append(this.clear);
        return sb.toString();
    }

    /**
     * Create a {@link Color} from the given byte array
     * @param data [CL, CH, RL, RH, GL, GR, BL, BH]
     * @return new {@link Color}
     */
    public static Color fromByteArray(byte[] data) {
        int clear = ((data[0] & 0xFF) | (data[1] << 8)) & 0xFFFF;
        int red   = ((data[2] & 0xFF) | (data[3] << 8)) & 0xFFFF;
        int green = ((data[4] & 0xFF) | (data[5] << 8)) & 0xFFFF;
        int blue  = ((data[6] & 0xFF) | (data[7] << 8)) & 0xFFFF;
        return new Color(red, green, blue, clear);
    }

    /**
     * Calculate lux (Illuminance) for the given RGB values
     * @param red sensor value
     * @param green sensor value
     * @param blue sensor value
     * @return lux
     */
    public static int toLux(int red, int green, int blue) {
        return (int) ((-0.32466f * (float) red) + (1.57837f * (float) green) + (-0.73191f * (float) blue));
    }

    public int toLux() {
        return toLux(red, green, blue);
    }

    /**
     * Calculate the Correlated Color Temperature (CCT) from the given
     * RGB values. Formula taken from http://ams.com/eng/content/view/download/145158
     * (TAOS Design Note 25 DN25)
     * @param red value from sensor
     * @param green value from sensor
     * @param blue value from sensor
     * @return CCT
     */
    public static int toColourTemperature(int red, int green, int blue) {
        float X, Y, Z, xc, yc, n;

        X = (-0.14282f * red) + (1.54924f * green) + (-0.95641f * blue);
        Y = (-0.32466f * red) + (1.57837f * green) + (-0.73191f * blue);
        Z = (-0.68202f * red) + (0.77073f * green) + ( 0.56332f * blue);

        xc = (X) / (X + Y + Z);
        yc = (Y) / (X + Y + Z);

        n = (xc - 0.3320F) / (0.1858F - yc);

        return (int) ((449.0F * Math.pow(n, 3)) + (3525.0F * Math.pow(n, 2)) + (6823.3F * n) + 5520.33F);
    }

    public int toColourTemperature() {
        return toColourTemperature(red, green, blue);
    }

    public int[] toIntArray() {
        int[] result = new int[4];
        result[0] = this.red;
        result[1] = this.green;
        result[2] = this.blue;
        result[3] = this.clear;
        return result;
    }

    static public Color toColor(int[] colorIntArray) {
        return new Color(
                colorIntArray[0],
                colorIntArray[1],
                colorIntArray[2],
                colorIntArray[3]);
    }
}
