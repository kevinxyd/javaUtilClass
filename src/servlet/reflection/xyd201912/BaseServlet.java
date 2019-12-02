package servlet.reflection.xyd201912;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created with IntelliJ IDEA.
 *
 * @Auther: xueYaDong
 * @Company: 东方标准
 * @Date: 2019/12/02/11:02
 * @Description:利用反射封装servlet请求
 */
public class BaseServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("service");

        //获取请求头地址/url/url/url
        String requestURI = request.getRequestURI();
        //分割地址
        String[] split = requestURI.split("/");
        //获取最后一个地址
        requestURI = split[split.length - 1];

        //通过反射获取调用对象
        Class aClass = this.getClass();
        try {
            //获取该对象的所有方法()参数一：方法名，参数二、三：该方法的参数类型
            Method method = aClass.getDeclaredMethod(requestURI, HttpServletRequest.class, HttpServletResponse.class);
            //暴力反射(针对私有方法)
            method.setAccessible(true);
            //动态调用方法
            method.invoke(this,request,response);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}