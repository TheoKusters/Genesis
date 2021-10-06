package web;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
@ComponentScan("web")
public class AppConfig implements AsyncConfigurer {

	public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        int corePoolSize = Integer.parseInt(System.getProperty("http.maxConnections", "5"));
        executor.setCorePoolSize(corePoolSize);
        executor.setKeepAliveSeconds(0);
        executor.setThreadNamePrefix("MyExecutor-");
        executor.initialize();
        return executor;
    }

	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		// TODO Auto-generated method stub
		return new SimpleAsyncUncaughtExceptionHandler() ;
	}

	public static Genesis init()  {
		ConfigurableApplicationContext context = new AnnotationConfigApplicationContext("web");
		Genesis genesis = context.getBean(Genesis.class);
		return genesis;
	}



}
