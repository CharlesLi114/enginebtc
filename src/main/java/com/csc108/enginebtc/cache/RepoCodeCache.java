package com.csc108.enginebtc.cache;

import com.csc108.enginebtc.exception.InitializationException;
import com.csc108.enginebtc.utils.ConfigUtil;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;

/**
 * Created by LI JT on 2019/9/6.
 * Description:
 */
public class RepoCodeCache {
    private static final Logger logger = LoggerFactory.getLogger(RepoCodeCache.class);


    public static RepoCodeCache RepoCache = new RepoCodeCache();


    private static final String SESSION_PROPERTY_FILE_NAME = "sessions.properties";
    private static final String REPO_CODE_PROPERTY_NAME = "Repo.Code";
    private static final String REPO_SPLITTER = ";";

    private Set<String> codes;

    private RepoCodeCache() {
        try {
            String configFile = ConfigUtil.getConfigPath(SESSION_PROPERTY_FILE_NAME);
            Configuration config = new PropertiesConfiguration(configFile);
            String[] repoCodes = config.getString(REPO_CODE_PROPERTY_NAME).split(REPO_SPLITTER);
            codes.addAll(Arrays.asList(repoCodes));
            logger.info("Preset Repo codes are: " + codes.toString());
        } catch (ConfigurationException e) {
            logger.error("Error during initializing repos.", e);
            throw new InitializationException("Error during initializing repos.", e);
        }
    }

    public boolean isRepo(String code) {
        return codes.contains(code);
    }



}
