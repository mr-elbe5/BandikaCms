/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.file;

import de.elbe5.application.ApplicationPath;
import de.elbe5.base.BinaryFile;
import de.elbe5.base.Log;
import de.elbe5.base.FileHelper;
import de.elbe5.content.ContentData;
import de.elbe5.database.DbBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileBean extends DbBean {

    private static final int DEFAULT_BUFFER_SIZE = 0x4000;

    private static FileBean instance = null;

    public static FileBean getInstance() {
        if (instance == null) {
            instance = new FileBean();
        }
        return instance;
    }

    public FileData getNewFileData(String className){
        try {
            Class<? extends FileData> cls = Class.forName(className).asSubclass(FileData.class);
            Constructor<? extends FileData> ctor = cls.getConstructor();
            return ctor.newInstance();
        }
        catch(Exception e){
            Log.error("could not create class " + className,  e);
        }
        return null;
    }

    public<T extends FileData> T getNewFileData(String className,Class<T> cls){
        FileData data = getNewFileData(className);
        return cls.cast(data);
    }

    public int getNextId() {
        return getNextId("s_file_id");
    }

    private static final String GET_ALL_FILES_SQL = "SELECT type,id,creation_date,change_date,parent_id,file_name,display_name,description,creator_id,changer_id,content_type,file_size FROM t_file order by file_name";

    public List<FileData> getAllFiles() {
        List<FileData> list = new ArrayList<>();
        Connection con = getConnection();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_ALL_FILES_SQL);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int i = 1;
                    String type = rs.getString(i++);
                    FileData data = getNewFileData(type);
                    if (data != null) {
                        data.setId(rs.getInt(i++));
                        data.setCreationDate(rs.getTimestamp(i++).toLocalDateTime());
                        data.setChangeDate(rs.getTimestamp(i++).toLocalDateTime());
                        data.setParentId(rs.getInt(i++));
                        data.setFileName(rs.getString(i++));
                        data.setDisplayName(rs.getString(i++));
                        data.setDescription(rs.getString(i++));
                        data.setCreatorId(rs.getInt(i++));
                        data.setChangerId(rs.getInt(i++));
                        data.setContentType(rs.getString(i++));
                        data.setFileSize(rs.getInt(i));
                    }
                    if (data!=null) {
                        FileBean extBean = data.getBean();
                        if (extBean != null)
                            extBean.readFileExtras(con, data, false);
                        list.add(data);
                    }
                }
            }
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
        return list;
    }

    public FileData getFile(int id,boolean complete) {
        FileData data = null;
        Connection con = getConnection();
        try {
            data = readFile(con, id, complete);
            FileBean extBean = data.getBean();
            if (extBean != null)
                extBean.readFileExtras(con, data, complete);
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeConnection(con);
        }
        return data;
    }

    public <T extends FileData> T getFile(int id, boolean complete, Class<T> cls) {
        try {
            return cls.cast(getFile(id,complete));
        }
        catch(NullPointerException | ClassCastException e){
            return null;
        }
    }

    private static final String GET_FILE_SQL = "SELECT type,id,creation_date,change_date,parent_id,file_name,display_name,description,creator_id,changer_id,content_type,file_size FROM t_file WHERE id=?";
    private static final String GET_FILE_COMPLETE_SQL = "SELECT type,id,creation_date,change_date,parent_id,file_name,display_name,description,creator_id,changer_id,content_type,file_size,bytes FROM t_file WHERE id=?";

    public FileData readFile(Connection con, int id, boolean complete) throws SQLException {
        FileData data = null;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(complete ? GET_FILE_COMPLETE_SQL : GET_FILE_SQL);
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int i = 1;
                    String type = rs.getString(i++);
                    data = getNewFileData(type);
                    if (data != null) {
                        data.setId(rs.getInt(i++));
                        data.setCreationDate(rs.getTimestamp(i++).toLocalDateTime());
                        data.setChangeDate(rs.getTimestamp(i++).toLocalDateTime());
                        data.setParentId(rs.getInt(i++));
                        data.setFileName(rs.getString(i++));
                        data.setDisplayName(rs.getString(i++));
                        data.setDescription(rs.getString(i++));
                        data.setCreatorId(rs.getInt(i++));
                        data.setChangerId(rs.getInt(i++));
                        data.setContentType(rs.getString(i++));
                        data.setFileSize(rs.getInt(i++));
                        if (complete){
                            data.setBytes(rs.getBytes(i));
                        }
                    }
                }
            }
        } finally {
            closeStatement(pst);
        }
        return data;
    }

    public void readFileExtras(Connection con, FileData fileData, boolean complete) throws SQLException{
    }

    public boolean saveFile(FileData data, boolean complete) {
        Connection con = startTransaction();
        try {
            if (!saveFile(con, data, complete))
                return rollbackTransaction(con);
            if (!commitTransaction(con))
                return false;
            if (complete) {
                writeStaticFile(data, true);
            }
            return true;
        } catch (Exception se) {
            return rollbackTransaction(con, se);
        }
    }

    public boolean saveFile(Connection con, FileData data, boolean complete) throws SQLException {
        writeFile(con, data, complete);
        FileBean extrasBean = data.getBean();
        if (extrasBean != null) {
            extrasBean.writeFileExtras(con, data, complete);
        }
        if (!data.isNew() && complete){
            writeStaticFile(data, true);
        }
        return true;
    }

    private static final String INSERT_FILE_SQL = "insert into t_file (type,creation_date,change_date,parent_id,file_name,display_name,description,creator_id,changer_id,content_type,file_size,bytes,id) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_FILE_SQL = "update t_file set type=?,creation_date=?,change_date=?,parent_id=?,file_name=?,display_name=?,description=?,creator_id=?,changer_id=?,content_type=?,file_size=?,bytes=? where id=?";
    private static final String UPDATE_FILE_NOBYTES_SQL = "update t_file set type=?,creation_date=?,change_date=?,parent_id=?,file_name=?,display_name=?,description=?,creator_id=?,changer_id=?,content_type=?,file_size=? where id=?";

    public void writeFile(Connection con, FileData data, boolean complete) throws SQLException {
        if (!data.isNew() && data.getBytes()==null)
            return;
        PreparedStatement pst;
        int i = 1;
        pst = con.prepareStatement(data.isNew() ? INSERT_FILE_SQL : (complete ? UPDATE_FILE_SQL : UPDATE_FILE_NOBYTES_SQL));
        pst.setString(i++, data.getClass().getName());
        pst.setTimestamp(i++, Timestamp.valueOf(data.getCreationDate()));
        pst.setTimestamp(i++, Timestamp.valueOf(data.getChangeDate()));
        if (data.getParentId() == 0) {
            pst.setNull(i++, Types.INTEGER);
        } else {
            pst.setInt(i++, data.getParentId());
        }
        pst.setString(i++, data.getFileName());
        pst.setString(i++, data.getDisplayName());
        pst.setString(i++, data.getDescription());
        pst.setInt(i++, data.getCreatorId());
        pst.setInt(i++, data.getChangerId());
        pst.setString(i++, data.getContentType());
        pst.setInt(i++, data.getFileSize());
        if (complete) {
            pst.setBytes(i++, data.getBytes());
        }
        pst.setInt(i, data.getId());
        pst.executeUpdate();
        pst.close();
    }

    public void writeFileExtras(Connection con, FileData contentData, boolean complete) throws SQLException {
    }

    private static final String GET_FILE_DATA_SQL = "SELECT file_name,content_type,file_size,bytes FROM t_file WHERE id=?";

    public BinaryFile getBinaryFile(int id) {
        Connection con = getConnection();
        PreparedStatement pst = null;
        BinaryFile data = null;
        try {
            pst = con.prepareStatement(GET_FILE_DATA_SQL);
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int i = 1;
                    data = new BinaryFile();
                    data.setFileName(rs.getString(i++));
                    data.setContentType(rs.getString(i++));
                    data.setFileSize(rs.getInt(i++));
                    data.setBytes(rs.getBytes(i));
                }
            }
        } catch (SQLException e) {
            Log.error("error while getting file", e);
            return null;
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
        return data;
    }

    public boolean assertFileDirectory(){
        File f = new File(ApplicationPath.getAppFilePath());
        if (f.exists()){
            return true;
        }
        return f.mkdir();
    }

    private static final String GET_FILE_STREAM_SQL = "SELECT file_size, bytes FROM t_file WHERE id=?";

    public boolean createTempFile(File file){
        Log.log("creating file " + file.getName());
        String fileName = file.getName();
        String name = FileHelper.getFileNameWithoutExtension(fileName);
        int id = Integer.parseInt(name);
        Connection con = getConnection();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_FILE_STREAM_SQL);
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    File f = new File(ApplicationPath.getAppFilePath(), fileName);
                    if (f.exists() && !f.delete()) {
                        Log.error("could not delete file " + f.getName());
                        return false;
                    }
                    if (!f.createNewFile())
                        throw new IOException("file create error");
                    FileOutputStream fout = new FileOutputStream(f);
                    long toRead = rs.getInt(1);
                    InputStream fin = rs.getBinaryStream(2);
                    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                    int read;
                    while ((read = fin.read(buffer)) > 0) {
                        if ((toRead -= read) > 0) {
                            fout.write(buffer, 0, read);
                        } else {
                            fout.write(buffer, 0, (int) toRead + read);
                            break;
                        }
                    }
                    fout.flush();
                    fout.close();
                }
            }
        } catch (SQLException | IOException e) {
            Log.error("error while getting file", e);
            return false;
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
        return true;
    }

    public boolean writeStaticFile(FileData data, boolean replace){
        String path = ApplicationPath.getAppFilePath()+"/"+data.getStaticFileName();
        if (!replace && FileHelper.fileExists(path))
            return true;
        return FileHelper.writeBinaryFile(path, data.getBytes());
    }

    public boolean deleteStaticFile(FileData data){
        String path = ApplicationPath.getAppFilePath()+"/"+data.getStaticFileName();
        return FileHelper.deleteBinaryFile(path);
    }

    private static final String DELETE_SQL = "DELETE FROM t_file WHERE id=?";

    public boolean deleteFile(FileData data) {
        if (!deleteItem(DELETE_SQL, data.getId())) {
            return false;
        }
        deleteStaticFile(data);
        return true;
    }

}
