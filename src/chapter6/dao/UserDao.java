package chapter6.dao;

import static chapter6.utils.CloseableUtil.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import chapter6.beans.User;
import chapter6.exception.NoRowsUpdatedRuntimeException;
import chapter6.exception.SQLRuntimeException;
import chapter6.logging.InitApplication;

public class UserDao {

	/**
	* ロガーインスタンスの生成
	*/
	Logger log = Logger.getLogger("twitter");

	/**
	* デフォルトコンストラクタ
	* アプリケーションの初期化を実施する。
	*/
	public UserDao() {
		InitApplication application = InitApplication.getInstance();
		application.init();

	}

	/*ユーザの登録*/
	public void insert(Connection connection, User user) {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		PreparedStatement ps = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO users ( ");
			sql.append("    account, ");
			sql.append("    name, ");
			sql.append("    email, ");
			sql.append("    password, ");
			sql.append("    description, ");
			sql.append("    created_date, ");
			sql.append("    updated_date ");
			sql.append(") VALUES ( ");
			sql.append("    ?, "); // account
			sql.append("    ?, "); // name
			sql.append("    ?, "); // email
			sql.append("    ?, "); // password
			sql.append("    ?, "); // description
			sql.append("    CURRENT_TIMESTAMP, "); // created_date
			sql.append("    CURRENT_TIMESTAMP "); // updated_date
			sql.append(")");

			ps = connection.prepareStatement(sql.toString());

			ps.setString(1, user.getAccount());
			ps.setString(2, user.getName());
			ps.setString(3, user.getEmail());
			ps.setString(4, user.getPassword());
			ps.setString(5, user.getDescription());

			ps.executeUpdate();
		} catch (SQLException e) {
			log.log(Level.SEVERE, new Object() {
			}.getClass().getEnclosingClass().getName() + " : " + e.toString(), e);
			throw new SQLRuntimeException(e);
		} finally {
			close(ps);
		}
	}

	/*ログイン時*/
	public User select(Connection connection, String accountOrEmail, String password) {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		PreparedStatement ps = null;
		try {
			String sql = "SELECT * FROM users WHERE (account = ? OR email = ?) AND password = ?";

			ps = connection.prepareStatement(sql);

			ps.setString(1, accountOrEmail);
			ps.setString(2, accountOrEmail);
			ps.setString(3, password);

			ResultSet rs = ps.executeQuery();

			List<User> users = toUsers(rs);
			if (users.isEmpty()) {
				return null;
			} else if (2 <= users.size()) {
				log.log(Level.SEVERE, "ユーザーが重複しています",
						new IllegalStateException());
				throw new IllegalStateException("ユーザーが重複しています");
			} else {
				return users.get(0);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, new Object() {
			}.getClass().getEnclosingClass().getName() + " : " + e.toString(), e);
			throw new SQLRuntimeException(e);
		} finally {
			close(ps);
		}
	}

	//toUsers…ResultSet(実行結果)をlist<User>に詰め替えている
	private List<User> toUsers(ResultSet rs) throws SQLException {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		List<User> users = new ArrayList<User>();
		try {
			while (rs.next()) {
				User user = new User();
				user.setId(rs.getInt("id"));
				user.setAccount(rs.getString("account"));
				user.setName(rs.getString("name"));
				user.setEmail(rs.getString("email"));
				user.setPassword(rs.getString("password"));
				user.setDescription(rs.getString("description"));
				user.setCreatedDate(rs.getTimestamp("created_date"));
				user.setUpdatedDate(rs.getTimestamp("updated_date"));

				users.add(user);
			}
			return users;
		} finally {
			close(rs);
		}
	}

	//ログイン機能
	public User select(Connection connection, int id) {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		PreparedStatement ps = null;
		try {
			String sql = "SELECT * FROM users WHERE id = ?";

			ps = connection.prepareStatement(sql);

			ps.setInt(1, id);

			ResultSet rs = ps.executeQuery();

			List<User> users = toUsers(rs);
			if (users.isEmpty()) {
				return null;
			} else if (2 <= users.size()) {
				log.log(Level.SEVERE, "ユーザーが重複しています", new IllegalStateException());
				throw new IllegalStateException("ユーザーが重複しています");
			} else {
				return users.get(0);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, new Object() {
			}.getClass().getEnclosingClass().getName() + " : " + e.toString(), e);
			throw new SQLRuntimeException(e);
		} finally {
			close(ps);
		}
	}

	//ユーザー情報の変更機能
	public void update(Connection connection, User user) {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		PreparedStatement ps = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE users SET ");
			sql.append("    account = ?, ");
			sql.append("    name = ?, ");
			sql.append("    email = ?, ");

			if (StringUtils.isNotBlank(user.getPassword())) {
				sql.append("    password = ?, ");
			}

			sql.append("    description = ?, ");
			sql.append("    updated_date = CURRENT_TIMESTAMP ");
			sql.append("WHERE id = ?");

			ps = connection.prepareStatement(sql.toString()); //実行

			ps.setString(1, user.getAccount());
			ps.setString(2, user.getName());
			ps.setString(3, user.getEmail());

			//パスワードに入力がない場合、パスワードは更新しない
			if (StringUtils.isBlank(user.getPassword())) {
				ps.setString(4, user.getDescription());
				ps.setInt(5, user.getId());
			} else {
				ps.setString(4, user.getPassword());
				ps.setString(5, user.getDescription());
				ps.setInt(6, user.getId());
			}

			int count = ps.executeUpdate();
			if (count == 0) {
				log.log(Level.SEVERE, "更新対象のレコードが存在しません", new NoRowsUpdatedRuntimeException());
				throw new NoRowsUpdatedRuntimeException();
			}

		} catch (SQLException e) {
			log.log(Level.SEVERE, new Object() {
			}.getClass().getEnclosingClass().getName() + " : " + e.toString(), e);
			throw new SQLRuntimeException(e);
		} finally {
			close(ps);
		}
	}

	//アカウントが重複して登録されないようにバリデーションを実装
	/*
	 * String型のaccountを引数にもつ、selectメソッドを追加する
	 */
	public User select(Connection connection, String account) {

		PreparedStatement ps = null;
		try {
			String sql = "SELECT * FROM users WHERE account = ?";

			ps = connection.prepareStatement(sql);
			ps.setString(1, account);

			//executeQuery()…sqlを実行 ResultSet…実行結果が入っている
			ResultSet rs = ps.executeQuery();

			//toUsers…rs(実行結果)をlist<User>に詰替え users…(詰替え後の)実行結果が入っている
			List<User> users = toUsers(rs);

			//データが無い状態＝重複していない場合はnullを返却
			if (users.isEmpty()) {
				return null;
			//(なぜかわからないが)既に重複が起きてしまっているので例外を投げ
			} else if (2 <= users.size()) {
				throw new IllegalStateException("ユーザーが重複しています");
			//1番目の要素あり＝1個既にある＝重複している場合は1番目の要素を返却
			} else {
				return users.get(0);
			}

		} catch (SQLException e) {
			throw new SQLRuntimeException(e);
		} finally {
			close(ps);
		}
	}
}
