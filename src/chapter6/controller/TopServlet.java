package chapter6.controller;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import chapter6.beans.User;
import chapter6.beans.UserComment;
import chapter6.beans.UserMessage;
import chapter6.logging.InitApplication;
import chapter6.service.CommentService;
import chapter6.service.MessageService;

@WebServlet(urlPatterns = { "/index.jsp" })
public class TopServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	* ロガーインスタンスの生成
	*/
	Logger log = Logger.getLogger("twitter");

	/**
	* デフォルトコンストラクタ
	* アプリケーションの初期化を実施する。
	*/
	public TopServlet() {
		InitApplication application = InitApplication.getInstance();
		application.init();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		/*セッションからログインユーザーのオブジェクトを取得できた場合・・・true
		  (ログインしていればtrue、していなければfalse)*/
		boolean isShowMessageForm = false;
		User user = (User) request.getSession().getAttribute("loginUser");
		if (user != null) {
			isShowMessageForm = true;
		}

		//特定のユーザーのつぶやきだけを表示
		/*
		 * String型のuser_idの値をrequest.getParameter("user_id")で
		 * JSPから受け取るように設定
		 * MessageServiceのselectに引数としてString型のuser_idを追加
		 */
		String userId = request.getParameter("user_id");

		//つぶやきの絞り込み（開始日と終了日を取得し、引数に設定）
		String start = request.getParameter("start");
		String end = request.getParameter("end");

		List<UserMessage> messages = new MessageService().select(userId, start, end);
		List<UserComment> comments = new CommentService().select();

		//jspに渡す
		request.setAttribute("start", start);
		request.setAttribute("end", end);
		request.setAttribute("messages", messages);
		request.setAttribute("comments", comments);
		request.setAttribute("isShowMessageForm", isShowMessageForm);
		request.getRequestDispatcher("/top.jsp").forward(request, response);
	}
}