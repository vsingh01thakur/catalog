package in.shabhushan.catalog.signin.servlet

import com.adobe.cq.account.api.AccountManagementService
import groovy.util.logging.Slf4j
import in.shabhushan.catalog.signin.constants.SigninConstants
import org.apache.commons.lang3.Validate
import org.apache.felix.scr.annotations.Component
import org.apache.felix.scr.annotations.Properties
import org.apache.felix.scr.annotations.Property
import org.apache.felix.scr.annotations.Reference
import org.apache.felix.scr.annotations.Service
import org.apache.jackrabbit.api.security.user.Authorizable
import org.apache.jackrabbit.api.security.user.UserManager
import org.apache.sling.api.SlingHttpServletRequest
import org.apache.sling.api.SlingHttpServletResponse
import org.apache.sling.api.request.RequestParameter
import org.apache.sling.api.resource.ResourceResolver
import org.apache.sling.api.resource.ResourceResolverFactory
import org.apache.sling.api.servlets.SlingAllMethodsServlet
import org.apache.sling.commons.json.JSONObject

import javax.jcr.Session
import javax.servlet.Servlet
import javax.servlet.ServletException

/**
 * The AccountManagementService provides a way for a visitor (a non logged-in user) to request a new account
 * or to request a password reset.
 * For any other account management task (e.g. modifying a user property)
 * use org.apache.jackrabbit.api.security.user.UserManager, based on the logged-in user session.
 *
 * Created by Shashi Bhushan
 *       on 9/4/17.
 */
// TODO: Change this to Resource Type
@Component
@Service(Servlet)
@Properties([
    @Property(name = "sling.servlet.paths", value="/bin/signup")
])
@Slf4j
class SignupServlet extends SlingAllMethodsServlet {

    @Reference
    AccountManagementService accountManagementService

    @Reference
    ResourceResolverFactory resourceResolverFactory

    private ResourceResolver resourceResolver
    boolean isAccountCreated

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
        throws ServletException, IOException {

        JSONObject jsonObject = new JSONObject()
        response.setContentType("application/json")

        String username = request.getParameter(SigninConstants.USERNAME_PLACEHOLDER)
        String password = request.getParameter(SigninConstants.PASSWORD_PLACEHOLDER)

        validateForNull(username, password)

        // Get Service Resource Resolver
        resourceResolver = getResourceResolver()

        // Check if user already exists
        Authorizable authorizable = getAuthorizableOf(username)

        RequestParameter[] parameters = new RequestParameter[1]
        parameters[0] = new CustomRequestParameter("stylesense3@gmail.com")

        // If there is no Authorizable of username
        if(!authorizable) {
            isAccountCreated = accountManagementService.requestAccount(username, password, ['email': parameters],
                        request.requestURL[0..-12], SigninConstants.PROPERTIES_NODE_PATH)

            jsonObject.put("status", isAccountCreated)
        } else {
            jsonObject.put("userExists" , true)
        }

//      TODO: Use AccountManagement API to update user

        response.writer.write(jsonObject.toString())
    }

    private void createUserInternal(SlingHttpServletRequest request) {


        ResourceResolver resourceResolver = request.resourceResolver

        Session session = resourceResolver.adaptTo(Session)
        UserManager userManager = resourceResolver.adaptTo(UserManager)

        log.debug("user ${username} with password ${password} has been created")

        if(userManager) {
            userManager.createUser(username, password)

            if(!userManager.autoSave) {
                session?.save()
            }
        }
    }

    /**
     * Get Service Resource Resolver associated with 'signupService'
     *
     * @return
     *      {@link ResourceResolver} of user associated with 'signupService'
     */
    private ResourceResolver getResourceResolver() {
        // Get Service Resource Resolver
        Map<String, Object> param = ["sling.service.subservice":"signupService"]

        return resourceResolverFactory.getServiceResourceResolver(param)
    }

    /**
     * Gets {@link Authorizable} of 'signupService' user
     *
     * @param username
     *      Username to get {@link Authorizable} of
     *
     * @return
     *      {@link Authorizable} instance of {@code username}
     */
    private Authorizable getAuthorizableOf(String username) {
        UserManager userManager = resourceResolver.adaptTo(UserManager)
        return userManager.getAuthorizable(username)
    }

    /**
     * Validate inputs for Null check using Apache common's Lang3 Library
     *
     * @param username
     *      Username of Signing up user
     *
     * @param password
     *      Password of Signing up user
     */
    private void validateForNull(String username, String password) {
        Validate.notNull(username, "Username is Mandatory")
        Validate.notNull(password, "Password is Mandatory")
    }

    private class CustomRequestParameter implements RequestParameter {

        private String email

        CustomRequestParameter(String email) {
            this.email = email
        }

        @Override
        String getName() {
            return null
        }

        @Override
        boolean isFormField() {
            return false
        }

        @Override
        String getContentType() {
            return null
        }

        @Override
        long getSize() {
            return 0
        }

        @Override
        byte[] get() {
            return new byte[0]
        }

        @Override
        InputStream getInputStream() throws IOException {
            return null
        }

        @Override
        String getFileName() {
            return null
        }

        @Override
        String getString() {
            return this.email
        }

        @Override
        String getString(String s) throws UnsupportedEncodingException {
            return this.email
        }
    }
}
