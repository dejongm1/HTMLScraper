package com.mcd.scraper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.List;


/**
 * Created by MikeyDizzle on 7/16/2017.
 */

@Configuration
@ComponentScan(basePackages = { "com.mcd.scraper.*" })
@PropertySource("file:${app.home}/config.properties")
public class Properties {

    @Value("#{'${user.agent.browsers}'.split(',')}")
    private List<String> browerAgents;

    @Value("#{'${user.agent.crawlers}'.split(',')}")
    private List<String> crawlerAgents;

    //To resolve ${} in @Value
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    public List<String> getBrowerAgents() {
        return browerAgents;
    }

    public List<String> getCrawlerAgents() {
        return crawlerAgents;
    }
}
