/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.stellar.bnkbiz.infrastructure.configuration.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import com.stellar.bnkbiz.infrastructure.configuration.data.ExternalServicesPropertiesData;
import com.stellar.bnkbiz.infrastructure.configuration.data.S3CredentialsData;
import com.stellar.bnkbiz.infrastructure.configuration.data.SMTPCredentialsData;
import com.stellar.bnkbiz.infrastructure.configuration.exception.ExternalServiceConfigurationNotFoundException;
import com.stellar.bnkbiz.infrastructure.core.service.RoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ExternalServicesPropertiesReadPlatformServiceImpl implements ExternalServicesPropertiesReadPlatformService {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ExternalServicesPropertiesReadPlatformServiceImpl(final RoutingDataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static final class S3CredentialsDataExtractor implements ResultSetExtractor<S3CredentialsData> {

        @Override
        public S3CredentialsData extractData(final ResultSet rs) throws SQLException, DataAccessException {
            String accessKey = null;
            String bucketName = null;
            String secretKey = null;
            while (rs.next()) {
                if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.S3_ACCESS_KEY)) {
                    accessKey = rs.getString("value");
                } else if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.S3_BUCKET_NAME)) {
                    bucketName = rs.getString("value");
                } else if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.S3_SECRET_KEY)) {
                    secretKey = rs.getString("value");
                }
            }
            return new S3CredentialsData(bucketName, accessKey, secretKey);
        }
    }

    private static final class SMTPCredentialsDataExtractor implements ResultSetExtractor<SMTPCredentialsData> {

        @Override
        public SMTPCredentialsData extractData(final ResultSet rs) throws SQLException, DataAccessException {
            String username = null;
            String password = null;
            String host = null;
            String port = "25";
            boolean useTLS = false;

            while (rs.next()) {
                if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.SMTP_USERNAME)) {
                    username = rs.getString("value");
                } else if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.SMTP_PASSWORD)) {
                    password = rs.getString("value");
                } else if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.SMTP_HOST)) {
                    host = rs.getString("value");
                } else if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.SMTP_PORT)) {
                    port = rs.getString("value");
                } else if (rs.getString("name").equalsIgnoreCase(ExternalServicesConstants.SMTP_USE_TLS)) {
                    useTLS = Boolean.parseBoolean(rs.getString("value"));
                }
            }
            return new SMTPCredentialsData(username, password, host, port, useTLS);
        }
    }

    private static final class ExternalServiceMapper implements RowMapper<ExternalServicesPropertiesData> {

        @Override
        public ExternalServicesPropertiesData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
            // TODO Auto-generated method stub
            final String name = rs.getString("name");
            String value = rs.getString("value");
            // Masking the password as we should not send the password back
            if (name != null && "password".equalsIgnoreCase(name)) {
                value = "XXXX";
            }
            return new ExternalServicesPropertiesData(name, value);
        }

    }

    @Override
    public S3CredentialsData getS3Credentials() {
        final ResultSetExtractor<S3CredentialsData> resultSetExtractor = new S3CredentialsDataExtractor();
        final String sql = "SELECT esp.name, esp.value FROM c_external_service_properties esp inner join c_external_service es on esp.external_service_id = es.id where es.name = '"
                + ExternalServicesConstants.S3_SERVICE_NAME + "'";
        final S3CredentialsData s3CredentialsData = this.jdbcTemplate.query(sql, resultSetExtractor, new Object[] {});
        return s3CredentialsData;
    }

    @Override
    public SMTPCredentialsData getSMTPCredentials() {
        // TODO Auto-generated method stub
        final ResultSetExtractor<SMTPCredentialsData> resultSetExtractor = new SMTPCredentialsDataExtractor();
        final String sql = "SELECT esp.name, esp.value FROM c_external_service_properties esp inner join c_external_service es on esp.external_service_id = es.id where es.name = '"
                + ExternalServicesConstants.SMTP_SERVICE_NAME + "'";
        final SMTPCredentialsData smtpCredentialsData = this.jdbcTemplate.query(sql, resultSetExtractor, new Object[] {});
        return smtpCredentialsData;
    }

    @Override
    public Collection<ExternalServicesPropertiesData> retrieveOne(String serviceName) {
        String serviceNameToUse = null;
        switch (serviceName) {
            case "S3":
                serviceNameToUse = ExternalServicesConstants.S3_SERVICE_NAME;
            break;

            case "SMTP":
                serviceNameToUse = ExternalServicesConstants.SMTP_SERVICE_NAME;
            break;

            default:
                throw new ExternalServiceConfigurationNotFoundException(serviceName);
        }
        final ExternalServiceMapper mapper = new ExternalServiceMapper();
        final String sql = "SELECT esp.name, esp.value FROM c_external_service_properties esp inner join c_external_service es on esp.external_service_id = es.id where es.name = '"
                + serviceNameToUse + "'";
        return this.jdbcTemplate.query(sql, mapper, new Object[] {});

    }
}
