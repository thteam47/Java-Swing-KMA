/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Admin
 */
public class NhanVien {
    int maNV;
    String tenNV;
    Date ngaySinh;
    String gioiTinh;
    String diaChi;

    public NhanVien() {
    }

    public NhanVien(int maNV, String tenNV, Date ngaySinh, String gioiTinh, String diaChi) {
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.diaChi = diaChi;
    }

    public int getMaNV() {
        return maNV;
    }

    public void setMaNV(int maNV) {
        this.maNV = maNV;
    }

    public String getTenNV() {
        return tenNV;
    }

    public void setTenNV(String tenNV) {
        this.tenNV = tenNV;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
    public String[] toArray(){
        String[] stringArray = {String.format("%03d", maNV), tenNV, new SimpleDateFormat("dd-MM-yyyy").format(ngaySinh), gioiTinh, diaChi};
        return stringArray;
    }
    public String dateToString(Date date)
    {
        return new SimpleDateFormat("dd-MM-yyyy").format(date);
    }
    public java.sql.Date utilDateToSQLDate(Date date)
    {
        return new java.sql.Date(date.getTime()); 
    }
    public Date sqlDateToUtilDate(java.sql.Date date)
    {
        return new java.util.Date(date.getTime()); 
    }
    
}
