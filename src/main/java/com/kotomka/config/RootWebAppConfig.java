package com.kotomka.config;

import com.kotomka.core.dao.AdminCoreDAO;
import com.kotomka.core.dao.JdbcAdminCoreDAO;
import com.kotomka.core.dao.JdbcShopCoreDAO;
import com.kotomka.core.dao.ShopCoreDAO;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;
import java.util.Properties;

@Configuration
@EnableWebMvc
@PropertySource("classpath:app.properties")
@ComponentScan(basePackages = "com.kotomka")
public class RootWebAppConfig implements WebMvcConfigurer {

    /**
     * <p>Объявление bean-а источника данных для выполнения запросов к бд.</p>
     */
    @Bean
    public DataSource dataSource(@Value("${db.url}") String dbUrl,
                                 @Value("${db.user.name}") String dbUserName,
                                 @Value("${db.user.password}") String dbUserPassword) {
        final ComboPooledDataSource ds = new ComboPooledDataSource();
        try {
            ds.setDriverClass(Driver.class.getName());
        } catch (final PropertyVetoException e) {
            throw new RuntimeException();
        }
        ds.setJdbcUrl(dbUrl);
        ds.setUser(dbUserName);
        ds.setPassword(dbUserPassword);
        ds.setNumHelperThreads(2);
        ds.setAcquireIncrement(2);
        ds.setInitialPoolSize(1);
        ds.setMinPoolSize(1);
        ds.setMaxPoolSize(5);
        ds.setIdleConnectionTestPeriod(300);
        ds.setMaxIdleTimeExcessConnections(300);
        return ds;
    }

    /**
     * <p>Объявление bean-ов для выполнения запросов к бд.</p>
     */
    @Bean
    @Autowired
    public ShopCoreDAO shopCoreDAO(DataSource dataSource) {
        return new JdbcShopCoreDAO(dataSource);
    }

    @Bean
    @Autowired
    public ShopCoreDAO shopCoreDAOForAdmin(DataSource dataSource) {
        JdbcShopCoreDAO jdbcShopCoreDAO = new JdbcShopCoreDAO(dataSource);
        jdbcShopCoreDAO.setAdminScope(true);
        return jdbcShopCoreDAO;
    }

    @Bean
    @Autowired
    public AdminCoreDAO adminCoreDAO(DataSource dataSource) {
        return new JdbcAdminCoreDAO(dataSource);
    }

    /**
     * <p>Объявление bean-а для получения отправки email сообщений.</p>
     */
    @Bean
    public JavaMailSenderImpl mailSender(
            @Value("${mail.host}") String emailServerHost,
            @Value("${mail.port}") int emailServerPort,
            @Value("${mail.username}") String emailServerUsername,
            @Value("${mail.password}") String emailServerPassword) {
        final Properties properties = new Properties();
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.ssl.enable", "false");
        properties.setProperty("mail.smtp.connectiontimeout", "10000");
        properties.setProperty("mail.smtp.timeout", "10000");

        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setJavaMailProperties(properties);
        mailSender.setHost(emailServerHost);
        mailSender.setPort(emailServerPort);
        mailSender.setUsername(emailServerUsername);
        mailSender.setPassword(emailServerPassword);

        return mailSender;
    }

    /**
     * <p>Настройка статического доступа к веб-ресурсам.</p>
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/*.html").addResourceLocations("/");
        registry.addResourceHandler("/store/**").addResourceLocations("/store/");
        registry.addResourceHandler("/admin/**").addResourceLocations("/admin/");
        registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
    }

    /**
     * <p>Настройка и маппинг url-адреса и соответствующего ему представления.</p>
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("/index.html");
        registry.addViewController("/admin/login").setViewName("/admin/login.jsp");
        registry.addViewController("/admin").setViewName("/admin/index.html");
    }

    /**
     * <p>Инициализация представлений (views).</p>
     */
    @Bean
    public InternalResourceViewResolver setupViewResolver() {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        return resolver;
    }

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver createMultipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSize(100000);
        resolver.setDefaultEncoding("utf-8");
        return resolver;
    }
}