package com.funnelback.publicui.search.lifecycle.input.processors.userkeys;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.funnelback.common.config.Config;
import com.funnelback.common.config.Keys;
import com.funnelback.publicui.search.model.collection.Collection;
import com.funnelback.publicui.search.model.transaction.SearchTransaction;

/**
 * <p>
 * This class generates the credentials for a specific user of the search engine
 * on Moodle.
 * </p>
 * 
 * @author mraymond
 * 
 */
public class MoodleMapper implements UserKeysMapper {

    static private Logger logger = Logger.getLogger(MoodleMapper.class);

    /**
     * Constant to get enrollments of a specific user
     */
    public static final String MOODLE_PARAMETER_NAME = "username";
    /**
     * Constant to separate the strings
     */
    private static final String SEPARATOR = "_";
    /**
     * The query which gets the list of courses where each user is enrolled, and
     * his role for each course
     */
    private static final String course = "SELECT mdl_user.username,  mdl_role.id as role,mdl_course.id as course FROM mdl_course, mdl_role_assignments, mdl_role, mdl_context, mdl_user where mdl_context.instanceid = mdl_course.id and mdl_context.id = mdl_role_assignments.contextid and mdl_role.id = mdl_role_assignments.roleid and mdl_user.id = mdl_role_assignments.userid order by username, role, course";
    /**
     * The query which gets exception about specific role for each module
     */
    private static final String module = "SELECT mdl_user.username,  mdl_role.id as role,mdl_course_modules.id as modules, mdl_course_modules.course FROM mdl_course_modules, mdl_context, mdl_role_assignments, mdl_role, mdl_user where mdl_context.instanceid = mdl_course_modules.id and mdl_context.id = mdl_role_assignments.contextid and mdl_role.id = mdl_role_assignments.roleid and mdl_user.id = mdl_role_assignments.userid and mdl_context.contextlevel = '70' order by username, role, course, modules";
    
    /**
     * Constructor empty
     */
    public MoodleMapper() {
    }
    
    /**
     * <p>
     * Method to get the userkeys for a Moodle user.
     * </p>
     * <p>
     * We need to connect the database and get the enrollments for the user.
     * </p>
     * 
     * @param transaction
     *            The way to get the configuration of the collection
     * @return The userkeys
     */
    public List<String> getUserKeys(Collection collection, SearchTransaction transaction) {

        List<String> rc = new ArrayList<String>();
        Connection connection = null;
        Statement statCourse = null;
        Statement statModule = null;

        try {

            // Get the configuration of the collection to reach the Moodle's db
            // And open database connection
            connection = this.getJdbcConnection(collection.getConfiguration());

            statCourse = connection.createStatement(
                    java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY);

            statModule = connection.createStatement(
                    java.sql.ResultSet.TYPE_FORWARD_ONLY,
                    java.sql.ResultSet.CONCUR_READ_ONLY);

            // Get the userkeys
            String userkeys = "";
    
            userkeys = this.makeUserkeys(transaction.getQuestion().getInputParameterMap()
                    .get(MOODLE_PARAMETER_NAME),
                    statCourse.executeQuery(course),
                    statModule.executeQuery(module));
            // Add userkeys as a list with one string
            rc.add(userkeys);

        } catch (Exception e) {
            logger.error("Unknown error while getting the userkeys", e);
        } finally {
            // Close the statements and the connection through the db
            if (connection != null) {
                this.closeConnection(connection);
            }
            if (statCourse != null) {
                this.closeStatement(statCourse);
            }
            if (statModule != null) {
                this.closeStatement(statModule);
            }
        }
        
        // Result
        return rc;
    }

    /**
     * <p>
     * Same method than the one used in a databse collection via the dbgather
     * code The connection through the Moodle's db is carried out.
     * </p>
     * 
     * @param configData
     * @return The connector
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws MalformedURLException
     */
    public final Connection getJdbcConnection(Config configData)
        throws ClassNotFoundException, SQLException, MalformedURLException {

        String jdbcClass = configData.value("db.jdbc_class");

        if (jdbcClass == null || jdbcClass.length() == 0) {
            throw new ClassNotFoundException("No db.jdbc_class in config");
        }

        Class.forName(jdbcClass);

        String jdbc_url = configData.value(Keys.Database.JDBC_ADDRESS);
        String username = configData.value(Keys.Database.JDBC_USERNAME);
        String password = configData.value(Keys.Database.JDBC_PASSWORD);

        return DriverManager.getConnection(jdbc_url, username, password);
    }

    /**
     * <p>
     * Method which closes the connection with the db.
     * </p>
     * 
     * @throws SQLException
     */
    public void closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            logger.warn("Could not close connection", e);
        }
    }

    /**
     * <p>
     * Method which closes a statement.
     * </p>
     * 
     * @throws SQLException
     */
    public void closeStatement(Statement stat) {
        try {
            stat.close();
        } catch (SQLException e) {
            logger.warn("Could not close statement", e);
        }
    }

    /**
     * <p>
     * Method which produces the credentials, basing on the course query.
     * </p>
     * 
     * @param username
     *            A specific user
     * @throws SQLException
     */
    public String makeUserkeys(String username, ResultSet queryCourse,
            ResultSet queryModule) throws SQLException {
        String rc = "";

        // Get the targeted result of the query & put it in a string
        // For the course only first
        while (queryCourse.next()) {
            if (queryCourse.getString("username").equalsIgnoreCase(username)) {
                try {
                    // Ensure course IDs are number
                    rc += "C" + Integer.parseInt(queryCourse.getString("course")) + SEPARATOR + "R"
                            + queryCourse.getString("role") + SEPARATOR;
                } catch (NumberFormatException nfe) {
                    logger.warn("Failed to parse courseId '"+queryCourse.getString("course")+"'", nfe);
                }
            }
        }
        // Then for the modules exceptions
        while (queryModule.next()) {
            if (queryModule.getString("username").equalsIgnoreCase(username)) {
                try {
                    // Ensure course and modules IDs are numbers
                    rc += "C" + Integer.parseInt(queryModule.getString("course")) + SEPARATOR + "M"
                        + Integer.parseInt(queryModule.getString("modules")) + SEPARATOR + "R"
                        + queryModule.getString("role") + SEPARATOR;
                } catch (NumberFormatException nfe) {
                    logger.warn("Failed to parse courseId '"+queryModule.getString("course")+"' or moduleId '"+queryModule.getString("modules")+"'");
                }
            }
        }

        // Log display
        logger.debug("Keys for the user '" + username + "' = '" + rc + "'");

        return rc;
    }
}
