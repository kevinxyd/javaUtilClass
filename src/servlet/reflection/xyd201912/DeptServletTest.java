package servlet.reflection.xyd201912;

import servlet.reflection.xyd201912.BaseServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: xueYaDong
 * @Company: 东方标准
 * @Date: 2019/12/02/11:07
 * @Description:
 */
@WebServlet("/sys/dept/*")
public class DeptServletTest extends BaseServlet {
    public void test1(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("dept/test1");
    }

    public void test2(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("dept/test2");
    }
}