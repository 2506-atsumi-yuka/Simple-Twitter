package chapter6.controller;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import chapter6.logging.InitApplication;
import chapter6.service.MessageService;

@WebServlet(urlPatterns = { "/deleteMessage" })
public class DeleteMessageServlet extends HttpServlet {
	/**
	* ロガーインスタンスの生成
	*/
	Logger log = Logger.getLogger("twitter");

	/**
	* デフォルトコンストラクタ
	* アプリケーションの初期化を実施する。
	*/
	public DeleteMessageServlet() {
		InitApplication application = InitApplication.getInstance();
		application.init();
	}

	//doPOSTで削除するデータを更新する
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		log.info(new Object() {
		}.getClass().getEnclosingClass().getName() +
				" : " + new Object() {
				}.getClass().getEnclosingMethod().getName());

		//top.jspから情報を受け取る ("deleteId"をint型に型変換)
		int deleteId = Integer.parseInt(request.getParameter("deleteId"));

		//serviceのdelete文を呼び出し →redirectで"/deleteMessage"へ移動
		new MessageService().delete(deleteId);
		response.sendRedirect("./");
	}
}
