package boardone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.sun.org.apache.regexp.internal.RE;

public class BoardDao {
	private static BoardDao instance = null;

	private BoardDao() {
	}

	public static BoardDao getInstance() {
		if (instance == null) {
			synchronized (BoardDao.class) {
				instance = new BoardDao();
			}
		}
		return instance;
	}

	public void insertArticle(BoardDto article) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int num = article.getNum();
		int ref = article.getRef();
		int step = article.getStep();
		int depth = article.getDepth();
		int number = 0;
		String sql = "";
		try {
			conn = ConnUtil.getConnection();
			pstmt = conn.prepareStatement("select max(NUM) from BOARD");
			rs = pstmt.executeQuery();
			if (rs.next()) {
				number = rs.getInt(1) + 1;
			} else {
				number = 1;
			}
			if (num != 0) {
				sql = "update BOARD set STEP=STEP+1 where REF = ? and STEP > ?";
				pstmt.close();
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, ref);
				pstmt.setInt(2, step);
				pstmt.executeQuery();
				step++;
				depth++;
			} else {
				ref = number;
				step = 0;
				depth = 0;
			}
			sql = "insert into BOARD" + "(NUM,WRITER,EMAIL,SUBJECT,PASS," + "REGDATE,REF,STEP,DEPTH,CONTENT,IP)"
					+ "values(BOARD_SEQ.nextval, ?,?,?,?,?,?,?,?,?,?)";
			pstmt.close();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, article.getWriter());
			pstmt.setString(2, article.getEmail());
			pstmt.setString(3, article.getSubject());
			pstmt.setString(4, article.getPass());
			pstmt.setTimestamp(5, article.getRegdate());
			pstmt.setInt(6, ref);
			pstmt.setInt(7, step);
			pstmt.setInt(8, depth);
			pstmt.setString(9, article.getContent());
			pstmt.setString(10, article.getIp());
			pstmt.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
				}
		}
	}

	public int getArticleCount() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int x = 0;
		try {
			conn = ConnUtil.getConnection();
			pstmt = conn.prepareStatement("select count(*) from BOARD");
			rs = pstmt.executeQuery();
			if (rs.next()) {
				x = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
				}
		}
		return x;
	}

	/*public List<BoardDto> getArticles() {*/
	public List<BoardDto> getArticles(int start,int end){
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<BoardDto> articleList = null;
		try {
			conn = ConnUtil.getConnection();
			/*수정2*/
			/*pstmt = conn.prepareStatement("select * from BOARD order by NUM desc");
			*/
			pstmt = conn.prepareStatement(
			"select * from (select rownum rnum, num, writer, email, subject, pass,"
					+"regdate, readcount, ref, step, depth, content, ip from("
					+"select * from board order by ref desc, step asc)) where rnum>=? and rnum<=?");
			pstmt.setInt(1, start);
			pstmt.setInt(2, end);
					
			rs = pstmt.executeQuery();
			if (rs.next()) {
				/*articleList = new ArrayList<BoardDto>();*/
				articleList = new ArrayList<BoardDto>(10);
				do {
					BoardDto article = new BoardDto();
					article.setNum(rs.getInt("num"));
					article.setWriter(rs.getString("writer"));
					article.setEmail(rs.getString("email"));
					article.setSubject(rs.getString("subject"));
					article.setPass(rs.getString("pass"));
					article.setRegdate(rs.getTimestamp("regdate"));
					article.setReadcount(rs.getInt("readcount"));
					article.setRef(rs.getInt("ref"));
					article.setStep(rs.getInt("step"));
					article.setDepth(rs.getInt("depth"));
					article.setContent(rs.getString("content"));
					article.setIp(rs.getString("ip"));
					articleList.add(article);
				} while (rs.next());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
				}
			// TODO: handle exception
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
				}
		}
		return articleList;
	}

	public BoardDto getArticle(int num) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		BoardDto article = null;
		try {
			conn = ConnUtil.getConnection();
			pstmt = conn.prepareStatement("update BOARD set READCOUNT=READCOUNT+1 where NUM = ?");
			pstmt.setInt(1, num);
			pstmt.executeUpdate();
			pstmt.close();
			pstmt = conn.prepareStatement("select * from BOARD where NUM = ?");
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				article = new BoardDto();
				article.setNum(rs.getInt("num"));
				article.setWriter(rs.getString("writer"));
				article.setEmail(rs.getString("email"));
				article.setSubject(rs.getString("subject"));
				article.setPass(rs.getString("pass"));
				article.setRegdate(rs.getTimestamp("regdate"));
				article.setReadcount(rs.getInt("readcount"));
				article.setRef(rs.getInt("ref"));
				article.setStep(rs.getInt("step"));
				article.setDepth(rs.getInt("depth"));
				article.setContent(rs.getString("content"));
				article.setIp(rs.getString("ip"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
				}
			// TODO: handle exception
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException e) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
				}
		}
		return article;
	}

public BoardDto updateGetArticle(int num) {
	Connection conn =null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	BoardDto article = null;
	try {
		conn = ConnUtil.getConnection();
		pstmt = conn.prepareStatement(
								"select * from BOARD where NUM = ?");
		pstmt.setInt(1, num);
		rs = pstmt.executeQuery();
		if(rs.next()) {
			article = new BoardDto();
			article.setNum(rs.getInt("num"));
			article.setWriter(rs.getString("writer"));
			article.setEmail(rs.getString("email"));
			article.setSubject(rs.getString("subject"));
			article.setPass(rs.getString("pass"));
			article.setRegdate(rs.getTimestamp("regdate"));
			article.setReadcount(rs.getInt("readcount"));
			article.setRef(rs.getInt("ref"));
			article.setStep(rs.getInt("step"));
			article.setDepth(rs.getInt("depth"));
			article.setContent(rs.getString("content"));
			article.setIp(rs.getString("ip"));
		}
	}catch (Exception e) {
		e.printStackTrace();
	} finally {
		if (rs != null)
			try {
				rs.close();
			} catch (SQLException e) {
			}
		// TODO: handle exception
		if (pstmt != null)
			try {
				pstmt.close();
			} catch (SQLException e) {
			}
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
			}
	}
	return article;
	}
public int updateArticle(BoardDto article) {
	Connection conn = null;
	PreparedStatement pstmt=null;
	ResultSet rs = null;
	String dbpassword="";
	String sql = "";
	int result = -1;
	try {
		conn = ConnUtil.getConnection();
		pstmt = conn.prepareStatement("select PASS from BOARD where NUM = ?");
		pstmt.setInt(1, article.getNum());
		rs = pstmt.executeQuery();
		if(rs.next()) {
			dbpassword = rs.getString("pass");
			if(dbpassword.equals(article.getPass())) {
				sql = "update BOARD set WRITER=?,EMAIL = ?,"
						+"SUBJECT = ?,CONTENT=? where NUM=?";
				pstmt.close();
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, article.getWriter());
				pstmt.setString(2, article.getEmail());
				pstmt.setString(3, article.getSubject());
				pstmt.setString(4, article.getContent());
				pstmt.setInt(5, article.getNum());
				pstmt.executeUpdate();
				result =1;
			}else {
				result =0;
			}
		}
	}catch (Exception e) {
		e.printStackTrace();
	}finally {
		if(rs != null)try {rs.close();}catch(SQLException e) {}
		if(pstmt != null) try {pstmt.close();}catch(SQLException e) {}
		if(conn != null) try {conn.close();}catch(SQLException e) {}
	}
	
	return result;
}
public int deleteArticle(int num,String pass) {
	Connection conn = null;
	PreparedStatement pstmt=null;
	ResultSet rs = null;
	String dbpassword="";
	int result = -1;
	try {
		conn = ConnUtil.getConnection();
		pstmt = conn.prepareStatement("select PASS from BOARD where NUM = ?");
		pstmt.setInt(1, num);
		rs = pstmt.executeQuery();
		if(rs.next()) {
			dbpassword = rs.getString("pass");
			if(dbpassword.equals(pass)) {
				pstmt.close();
				pstmt = conn.prepareStatement("delete from BOARD where NUM=?");
				pstmt.setInt(1, num);
				pstmt.executeUpdate();
				result =1;
			}else {
				result =0;
			}
		}
	}catch (Exception e) {
		e.printStackTrace();
	}finally {
		if(rs != null)try {rs.close();}catch(SQLException e) {}
		if(pstmt != null) try {pstmt.close();}catch(SQLException e) {}
		if(conn != null) try {conn.close();}catch(SQLException e) {}
	}
	
	return result;
}
}
