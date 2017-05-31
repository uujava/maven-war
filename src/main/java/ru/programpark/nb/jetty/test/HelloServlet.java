package ru.programpark.nb.jetty.test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;

public class HelloServlet extends HttpServlet {
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        resp.setContentType("text/plain");
        String aClass = req.getParameter("class");
        if (aClass == null) {
            aClass = "ru.programpark.nb.jetty.test.HelloServletVersion";
        }
        try {
            writeVersion(writer, aClass);
        } catch (Throwable e) {
            writeException(writer, e);
        }
        writer.flush();
        writer.close();
    }

    private void writeException(PrintWriter writer, Throwable e) {
        e.printStackTrace(writer);
    }

    private void writeVersion(PrintWriter writer, String clazz) throws IOException {
        writer.println("Version: " + HelloServletVersion.getVersion());
        printResources(writer, "ThreadClassLoader", Thread.currentThread().getContextClassLoader(), clazz);
        printResources(writer, "WebApp", HelloServlet.class.getClassLoader(), clazz);
        printResources(writer, "System", System.class.getClassLoader(), clazz);
        printResources(writer, "Servlet", HttpServlet.class.getClassLoader(), clazz);
        printResources(writer, "JRuby", getJRubyClassLoader(writer), clazz);
    }

    private ClassLoader getJRubyClassLoader(PrintWriter writer) {
        try{
            Class<?> jruby = Class.forName("org.jruby.Ruby");
            Method getGlobalRuntime = jruby.getMethod("getGlobalRuntime");
            Object runtime = getGlobalRuntime.invoke(null);
            Method getJRubyClassLoader = runtime.getClass().getMethod("getJRubyClassLoader");
            return (ClassLoader) getJRubyClassLoader.invoke(runtime);
        }catch(Throwable ex){
            writeException(writer, ex);
        }
        return null;
    }

    private void printResources(PrintWriter writer, String loaderName, ClassLoader classLoader, String clazz) {
        writer.println("**** " + loaderName +" ***************");
        if(classLoader != null) {
            writer.println("Class: " + clazz);
            writer.println("ClassLoader: " + classLoader);
            writer.println("FirstPath: " + classLoader.getResource(clazz.replace('.', '/') + ".class"));
            try {
                writer.println("AllPaths: ");
                Enumeration<URL> resources = classLoader.getResources(clazz.replace('.', '/') + ".class");
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    writer.println("paths: " + url);
                }
            } catch (Throwable ex) {
                writer.println("Exception list all paths: " + clazz);
                writeException(writer, ex);
            }
            try {
                classLoader.loadClass(clazz);
            } catch (ClassNotFoundException e) {
                writer.println("Exception loading class: " + clazz + " from " + classLoader);
                writeException(writer, e);
            }
        } else {
            writer.println("loader is null");
        }
    }
}
