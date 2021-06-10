module DBConnector {
    requires jakarta.persistence;
    requires java.sql;
    opens domain to com.google.gson;
    exports domain;
    exports sourcepackage;

}