package controllers;

import Config.SQLServerConnect;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import models.NhanVien;
import views.NhanVienFrame;
import views.NhanVienJdialog;

/**
 *
 * @author Admin
 */
public class NhanVienController {

    private final String[] tableHeaders = {"Mã nhân viên", "Tên nhân viên", "Ngày sinh", "Giới tính", "Địa chỉ"};
    SQLServerConnect sqlServerConnect;
    Connection connection;
    private static TableRowSorter<TableModel> rowSorter = null;
    NhanVienFrame view = null;
    NhanVienJdialog viewNV = null;
    NhanVien model = null;

    public NhanVienController() {
        sqlServerConnect = new SQLServerConnect();
        connection = sqlServerConnect.connect();
        this.view = new NhanVienFrame();
        view.setVisible(true);
        view.setLocationRelativeTo(null);
        setHeaderForTable();
        getDataTotable();
        rowSorter = new TableRowSorter<>(view.getTblBang().getModel());
        view.getTblBang().setRowSorter(rowSorter);
        view.getjtfSearch().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                String text = view.getjtfSearch().getText();
                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                String text = view.getjtfSearch().getText();
                if (text.trim().length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });

        view.getAdd().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewNV = new NhanVienJdialog(main.app.mainFrame, true);
                viewNV.getTxtMaKH().setEditable(false);

                viewNV.getBtnThem().addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        insertDataToDB();
                        getDataTotable();
                    }
                });
                viewNV.getBtnReset().addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        reset();
                    }
                });
                viewNV.setLocationRelativeTo(null);
                viewNV.setResizable(false);
                viewNV.setVisible(true);
            }
        });
        view.getTblBang().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && view.getTblBang().getSelectedRow() != -1) {
                    DefaultTableModel model = (DefaultTableModel) view.getTblBang().getModel();
                    int selectedRowIndex = view.getTblBang().convertRowIndexToModel(view.getTblBang().getSelectedRow());

                    String maNV = model.getValueAt(selectedRowIndex, 0).toString() != null
                            ? model.getValueAt(selectedRowIndex, 0).toString() : "";
                    String hoTen = model.getValueAt(selectedRowIndex, 1).toString() != null
                            ? model.getValueAt(selectedRowIndex, 1).toString() : "";
                    String diaChi = model.getValueAt(selectedRowIndex, 4).toString() != null
                            ? model.getValueAt(selectedRowIndex, 4).toString() : "";

                    String gioitinh = model.getValueAt(selectedRowIndex, 3).toString() != null
                            ? model.getValueAt(selectedRowIndex, 3).toString() : "";

                    Date ngaySinh = null;
                    try {
                        ngaySinh = new java.text.SimpleDateFormat("dd-MM-yyyy").parse(model.getValueAt(selectedRowIndex, 2).toString());
                    } catch (ParseException ex) {
                        Logger.getLogger(NhanVienController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    NhanVien nv = new NhanVien(Integer.parseInt(maNV), hoTen, ngaySinh, gioitinh, diaChi);

                    viewNV = new NhanVienJdialog(main.app.mainFrame, true);

                    setModel(nv);
                    viewNV.getBtnThem().addActionListener(al -> btnSuaPerformed());
                    viewNV.getBtnReset().addActionListener(al -> reset());
                    viewNV.setLocationRelativeTo(null);
                    viewNV.setResizable(false);
                    viewNV.setVisible(true);

                }
            }

            //Hiển thị Jpopupmenu khi chọn để sửa hoặc xoá
            @Override
            public void mouseReleased(MouseEvent e) {
                int r = view.getTblBang().rowAtPoint(e.getPoint());
                if (r >= 0 && r < view.getTblBang().getRowCount()) {
                    view.getTblBang().setRowSelectionInterval(r, r);
                } else {
                    view.getTblBang().clearSelection();
                }

                //row index is found...
                int rowindex = view.getTblBang().getSelectedRow();
                if (rowindex < 0) {
                    return;
                }
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                    JPopupMenu popup = createEditAndDeletePopUp(rowindex, view.getTblBang());
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }

        });

        view.getTblBang().getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 12));
        view.getTblBang().getTableHeader().setPreferredSize(new Dimension(100, 50));
        view.getTblBang().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        view.getTblBang().setRowHeight(50);
        view.getTblBang().validate();
        view.getTblBang().repaint();
    }

    private void setHeaderForTable() {
        view.getDtm().setColumnIdentifiers(tableHeaders);
    }

    public void getDataTotable() {
        try {
            view.getDtm().setRowCount(0);
            String sqlQuery = "SELECT * FROM NHANVIEN";
            PreparedStatement ps = connection.prepareStatement(sqlQuery);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int maNV = rs.getInt("maNV");
                String tenNV = rs.getString("hoTen");
                String gioitinh = rs.getString("gioitinh");
                Date ngaysinh = new java.sql.Date(rs.getDate("ngaysinh").getTime());
                String diaChi = rs.getString("diachi");
                NhanVien nv = new NhanVien(maNV, tenNV, ngaysinh, gioitinh, diaChi);
                view.getDtm().addRow(nv.toArray());
            }
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(NhanVienController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void reset() {
        viewNV.getTxtDiaChi().setText("");
        viewNV.getTxtTen().setText("");
        viewNV.getTxtNgaySinh().setDate(new Date());
        viewNV.getGrpGioiTinh().clearSelection();
    }

    public void insertDataToDB() {
        model = getModel();
        if (model != null) {
            try {

                String sqlQueryInsert = "INSERT NHANVIEN(hoTen,ngaySinh,gioiTinh, diaChi) "
                        + "VALUES (?, ? ,? ,? )";
                PreparedStatement ps = connection.prepareStatement(sqlQueryInsert);
                ps.setString(1, model.getTenNV());
                ps.setDate(2, model.utilDateToSQLDate(model.getNgaySinh()));
                ps.setString(3, model.getGioiTinh());
                ps.setString(4, model.getDiaChi());

                int result = ps.executeUpdate();
                if (result == 1) {
                    {
                        JOptionPane.showMessageDialog(view, "Thêm thành công!");
                        reset();
                    }
                } else {
                    JOptionPane.showMessageDialog(view, "Thêm thất bại!");
                }
                ps.close();
            } catch (SQLException ex) {
                if (ex.toString().contains("PRIMARY KEY")) {
                    JOptionPane.showMessageDialog(view, "Trùng khoá chính!");
                } else if (ex.toString().contains("String or binary data would be truncated")) {
                    JOptionPane.showMessageDialog(view, "Không thể để 1 trường quá dài!");
                } else {
                    Logger.getLogger(NhanVienController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    public void btnSuaPerformed() {
        model = getModel();
        if (model != null) {
            try {
                String sql = "UPDATE [dbo].[NHANVIEN]"
                        + "       SET"
                        + "       [hoTen] = ?"
                        + "      ,[diaChi] = ?"
                        + "      ,[gioiTinh] = ?"
                        + "      ,[ngaySinh] = ?"
                        + " WHERE maNV = ?";
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, model.getTenNV());
                ps.setString(2, model.getDiaChi());
                ps.setString(3, model.getGioiTinh());
                ps.setDate(4, model.utilDateToSQLDate(model.getNgaySinh()));
                ps.setInt(5, model.getMaNV());
                int result = ps.executeUpdate();
                if (result == 1) {
                    JOptionPane.showMessageDialog(view, "Thay đổi giá trị thành công!");
                    viewNV.dispose();
                }
                getDataTotable();
                ps.close();

            } catch (SQLException ex) {
                if (ex.toString().contains("String or binary data would be truncated")) {
                    JOptionPane.showMessageDialog(view, "Không thể để 1 trường quá dài!");
                } else {
                    Logger.getLogger(NhanVienController.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(view, "Không thể cập nhật giá trị! \nCó lỗi xảy ra! ");
                }
            }
        }
    }

    public NhanVien getModel() {
        int maNVI = 0;
        if (viewNV.getTxtTen().getText().isEmpty() || viewNV.getTxtDiaChi().getText().isEmpty()) {
            JOptionPane.showMessageDialog(viewNV, "Phải điền tất cả thông tin!");
        } else {
            String diaChi = viewNV.getTxtDiaChi().getText();
            String maNV = viewNV.getTxtMaKH().getText();
            if (!maNV.equals("")){
                maNVI = Integer.parseInt(maNV);
            }
            String gioiTinh = viewNV.getRdNam().isSelected() ? "Nam" : "Nữ";
            Date ngayDK = new Date();
            Date ngaySinhS = viewNV.getTxtNgaySinh().getDate();
            String ten = viewNV.getTxtTen().getText();
            return new NhanVien(maNVI, ten, ngaySinhS, gioiTinh, diaChi);
        }
        return null;
    }

    public JPopupMenu createEditAndDeletePopUp(int rowindex, JTable table) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Chỉnh sửa");
        edit.setIcon(new javax.swing.ImageIcon(NhanVienController.class.getResource("/images/edit.png"))); // NOI18N
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                int selectedRowIndex = table.convertRowIndexToModel(table.getSelectedRow());

                String maNV = model.getValueAt(selectedRowIndex, 0).toString() != null
                        ? model.getValueAt(selectedRowIndex, 0).toString() : "";
                String hoTen = model.getValueAt(selectedRowIndex, 1).toString() != null
                        ? model.getValueAt(selectedRowIndex, 1).toString() : "";
                String diaChi = model.getValueAt(selectedRowIndex, 4).toString() != null
                        ? model.getValueAt(selectedRowIndex, 4).toString() : "";

                String gioitinh = model.getValueAt(selectedRowIndex, 3).toString() != null
                        ? model.getValueAt(selectedRowIndex, 3).toString() : "";

                Date ngaySinh = null;
                try {
                    ngaySinh = new java.text.SimpleDateFormat("dd-MM-yyyy").parse(model.getValueAt(selectedRowIndex, 2).toString());
                } catch (ParseException ex) {
                    Logger.getLogger(NhanVienController.class.getName()).log(Level.SEVERE, null, ex);
                }

                NhanVien nv = new NhanVien(Integer.parseInt(maNV), hoTen, ngaySinh, gioitinh, diaChi);

                viewNV = new NhanVienJdialog(main.app.mainFrame, true);

                setModel(nv);
                viewNV.getBtnThem().addActionListener(al -> btnSuaPerformed());
                viewNV.getBtnReset().addActionListener(al -> reset());
                viewNV.setLocationRelativeTo(null);
                viewNV.setResizable(false);
                viewNV.setVisible(true);

            }
        });
        JMenuItem delete = new JMenuItem("Xoá");
        delete.setIcon(new javax.swing.ImageIcon(NhanVienController.class.getResource("/images/delete.png"))); // NOI18N
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int dialogResult = JOptionPane.showConfirmDialog(main.app.mainFrame,
                        "Bạn muốn xoá nhân viên này không?", "Xác nhận xoá", JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    int selectedRowIndex = table.convertRowIndexToModel(table.getSelectedRow());
                    String maNV = model.getValueAt(selectedRowIndex, 0).toString() != null
                            ? model.getValueAt(selectedRowIndex, 0).toString() : "";
                    String hoTen = model.getValueAt(selectedRowIndex, 1).toString() != null
                            ? model.getValueAt(selectedRowIndex, 1).toString() : "";
                    String diaChi = model.getValueAt(selectedRowIndex, 4).toString() != null
                            ? model.getValueAt(selectedRowIndex, 4).toString() : "";
                    try {
                        String sqlQuery = "DELETE FROM NHANVIEN WHERE maNV=?";

                        PreparedStatement ps = connection.prepareStatement(sqlQuery);
                        ps.setInt(1, Integer.parseInt(maNV));
                        int result = ps.executeUpdate();
                        if (result == 1) {

                            JOptionPane.showMessageDialog(view, "Xóa thành công!");
                        } else {

                        }
                        getDataTotable();
                        ps.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(NhanVienController.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        });
        popup.add(edit);
        popup.add(delete);
        return popup;
    }

    public void setModel(NhanVien nv) {
        viewNV.getTxtTen().setText(nv.getTenNV());
        viewNV.getTxtMaKH().setText(String.format("%03d", nv.getMaNV()));
        viewNV.getTxtMaKH().setEditable(false);
        viewNV.getTxtDiaChi().setText(nv.getDiaChi());
        if (nv.getGioiTinh().equals("Nam")) {
            viewNV.getRdNam().setSelected(true);
        } else {
            viewNV.getRdNu().setSelected(true);
        }
        viewNV.getTxtNgaySinh().setDate(nv.getNgaySinh());

    }

    public static void main(String[] args) {

        new NhanVienController();
    }

}
