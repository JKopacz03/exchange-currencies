package com.kopacz.JAROSLAW_KOPACZ_TEST_3;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.Value;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static liquibase.database.DatabaseFactory.*;

@Service
@ActiveProfiles("test")
@Value
public class DatabaseCleaner {

    Connection connection;
    Database database;
    Liquibase liquibase;

    public DatabaseCleaner() throws DatabaseException, SQLException {
        this.connection = DriverManager.getConnection("jdbc:h2:mem:test","user","password");
        this.database = getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
        this.liquibase = new Liquibase("liquibase-test-changelog.xml", new ClassLoaderResourceAccessor(), database);
    }

    public void cleanUp() throws LiquibaseException {
        liquibase.dropAll();
        liquibase.update(null,new Contexts());
    }
}
