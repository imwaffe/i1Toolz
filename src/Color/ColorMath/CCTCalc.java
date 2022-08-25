package Color.ColorMath;

public class CCTCalc {

    public static int McCamy(Double[] xyY){
        double n = (xyY[0]-0.3320)/(0.1858-xyY[1]);
        double CCT = 437*Math.pow(n,3) + 3601*Math.pow(n,2) + 6861*n + 5517;
        return (int) Math.round(CCT);
    }

}
