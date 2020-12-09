package com.ch.cloud.kafka.utils;

import com.alibaba.fastjson.JSON;
import com.ch.Constants;
import com.ch.utils.CommonUtils;
import com.google.common.collect.Lists;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;

import java.math.BigDecimal;
import java.util.List;

/**
 * decs:
 *
 * @author 01370603
 * @date 2020/12/8
 */
public class MapUtils {

    private static final double EARTH_RADIUS = 6378.137;


    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }


    /**
     * 根据两点间经纬度坐标（double值），计算两点间距离，单位为米
     *
     * @param lng1
     * @param lat1
     * @param lng2
     * @param lat2
     * @return
     */
    public static double calcDistance(Double lng1, Double lat1, Double lng2, Double lat2) {
        if (null == lng1 || null == lat1 || null == lng2 || null == lat2) {
            return 0;
        }

        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        // s = Math.round(s * 10000) / 10000;

        BigDecimal decimal = new BigDecimal(s * 1000);
        return decimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        //return s;
    }


    public static double calcDistance(String point1, String point2) {
        if (CommonUtils.isEmptyOr(point1, point2)) {
            return 0;
        }
        String[] arr1 = point1.split(Constants.SEPARATOR_2);
        String[] arr2 = point2.split(Constants.SEPARATOR_2);
        double p1x = Double.parseDouble(arr1[0]);
        double p1y = Double.parseDouble(arr1[1]);
        double p2x = Double.parseDouble(arr2[0]);
        double p2y = Double.parseDouble(arr1[1]);

        return calcDistance(p1x, p1y, p2x, p2y);
    }

    public static double[] add(String point1, String point2, double currD) {

        double[] p1 = parsePoint(point1);
        double[] p2 = parsePoint(point2);
        GeodeticCalculator calculator = new GeodeticCalculator();

        GlobalCoordinates start = new GlobalCoordinates(p1[0], p1[1]);
        GlobalCoordinates point = calculator.calculateEndingGlobalCoordinates(Ellipsoid.Sphere, start, 45, currD, p2);

        return new double[]{point.getLongitude(), point.getLatitude()};
    }

    public static double getDistanceMeter(GlobalCoordinates gpsFrom, GlobalCoordinates gpsTo, Ellipsoid ellipsoid) {

        //创建GeodeticCalculator，调用计算方法，传入坐标系、经纬度用于计算距离
        GeodeticCurve geoCurve = new GeodeticCalculator().calculateGeodeticCurve(ellipsoid, gpsFrom, gpsTo);


        return geoCurve.getEllipsoidalDistance();
    }


    public static double[] parsePoint(String point) {
        String[] arr1 = point.split(Constants.SEPARATOR_2);
        double p1x = Double.parseDouble(arr1[0]);
        double p1y = Double.parseDouble(arr1[1]);
        return new double[]{p1x, p1y};
    }

    public static void main(String[] args) {
        String point1 = "113.929208, 22.50641";
        String point2 = "114.034705, 22.616348";

        double d = calcDistance(point1, point2);

//        GlobalCoordinates source = new GlobalCoordinates(29.490295, 106.486654);
        GlobalCoordinates source = new GlobalCoordinates(113.929208, 22.50641);
        GlobalCoordinates target = new GlobalCoordinates(114.034705, 22.616348);

        double meter1 = getDistanceMeter(source, target, Ellipsoid.Sphere);
        double meter2 = getDistanceMeter(source, target, Ellipsoid.WGS84);
        double[] meter3 = add("113.929208,22.50641", "114.034705,22.616348", 12739);

        System.out.println("Code代码 坐标系计算结果：" + d + "米");
        System.out.println("Sphere 坐标系计算结果：" + meter1 + "米");
        System.out.println("WGS84 坐标系计算结果：" + meter2 + "米");
        System.out.println("坐标系计算结果：" + meter3[0] + "," + meter3[1]);

        List<double[]> points11 = calcGPS(point1, point2, 11);

        System.out.println("11 calcGPS: " + JSON.toJSONString(points11));
    }


    private static List<double[]> calcGPS(String point1, String point2, int total) {

        double[] p1 = parsePoint(point1);
        double[] p2 = parsePoint(point2);

        double x_ = (p2[0] - p1[0])/total;
        double y_ = (p2[1] - p1[1])/total;

        List<double[]> list = Lists.newArrayList();
        for (int i = 0; i < total; i++) {
            double x = p1[0], y = p1[1];
            double offsetX = x_ * i;
            double offsetY = y_ * i;

            list.add(new double[]{x + offsetX, y + offsetY});
        }

        list.add(p2);
        return list;
    }
}
