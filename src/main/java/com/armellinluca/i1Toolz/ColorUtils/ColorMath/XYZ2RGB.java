package com.armellinluca.i1Toolz.ColorUtils.ColorMath;

public class XYZ2RGB {
    private final double[][] forwardMatrix;
    private final double[][] backwardMatrix;
    private final double gamma;
    private final double inverseGamma;

    public XYZ2RGB(double[][] forwardMatrix, double gamma){
        this.forwardMatrix = forwardMatrix;
        backwardMatrix = invertMatrix(forwardMatrix);
        this.gamma = gamma;
        inverseGamma = 1/gamma;
    }

    public double[] rgb2xyz(int[] rgb){
        double[] linearRgb = new double[3];
        for(int i=0; i<3; i++)
            linearRgb[i] = Math.pow(rgb[i]/255D, gamma);
        return productMatrixColumnVector(forwardMatrix, linearRgb);
    }

    public int[] xyz2rgb(double[] xyz){
        int[] rgb = new int[3];
        double[] linearRgb = productMatrixColumnVector(backwardMatrix, xyz);
        for(int i=0; i<3; i++)
            rgb[i] = (int)Math.round(Math.pow(linearRgb[i]/100D, inverseGamma)*255D);
        return rgb;
    }

    public int[] xyz2rgb(Float[] xyz){
        double[] doubleXYZ = {xyz[0], xyz[1], xyz[2]};
        return xyz2rgb(doubleXYZ);
    }

    private static double[] productMatrixColumnVector(double[][] matrix, double[] columnVector){
        double[] result = new double[3];
        for(int r=0; r<3; r++)
            result[r] = matrix[r][0]*columnVector[0]+matrix[r][1]*columnVector[1]+matrix[r][2]*columnVector[2];
        return result;
    }

    public static int[] boundRGBValues(int[] rgb){
        return new int[]{Math.min(rgb[0],0xFF),Math.min(rgb[1],0xFF),Math.min(rgb[2],0xFF)};
    }

    public static double[][] invertMatrix(double[][] matrix){
        int n = matrix.length;
        double[][] inverted = new double[n][n];
        double[][] b = new double[n][n];
        int[] index = new int[n];
        for (int i=0; i<n; ++i)
            b[i][i] = 1;
        gaussianReduction(matrix, index);
        for(int i=0; i<n-1; ++i)
            for(int j=i+1; j<n; ++j)
                for(int k=0; k<n; ++k)
                    b[index[j]][k] -= matrix[index[j]][i]*b[index[i]][k];
        for(int i=0; i<n; ++i){
            inverted[n-1][i] = b[index[n-1]][i]/matrix[index[n-1]][n-1];
            for(int j=n-2; j>=0; --j){
                inverted[j][i] = b[index[j]][i];
                for(int k=j+1; k<n; ++k)
                    inverted[j][i] -= matrix[index[j]][k]*inverted[k][i];
                inverted[j][i] /= matrix[index[j]][j];
            }
        }
        return inverted;
    }

    private static void gaussianReduction(double[][] a, int[] index){
        int n = index.length;
        double[] c = new double[n];
        for (int i=0; i<n; ++i)
            index[i] = i;
        for (int i=0; i<n; ++i) {
            double c1 = 0;
            for (int j=0; j<n; ++j){
                double c0 = Math.abs(a[i][j]);
                if (c0 > c1) c1 = c0;
            }
            c[i] = c1;
        }
        int k = 0;
        for(int j=0; j<n-1; ++j){
            double pi1 = 0;
            for(int i=j; i<n; ++i){
                double pi0 = Math.abs(a[index[i]][j]);
                pi0 /= c[index[i]];
                if(pi0 > pi1){
                    pi1 = pi0;
                    k = i;
                }
            }
            int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
            for(int i=j+1; i<n; ++i){
                double pj = a[index[i]][j]/a[index[j]][j];
                a[index[i]][j] = pj;
                for(int l=j+1; l<n; ++l)
                    a[index[i]][l] -= pj*a[index[j]][l];
            }
        }
    }
}