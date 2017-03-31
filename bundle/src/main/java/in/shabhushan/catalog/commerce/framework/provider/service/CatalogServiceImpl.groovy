/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.shabhushan.catalog.commerce.framework.provider.service

import com.adobe.cq.commerce.api.*
import com.adobe.cq.commerce.common.AbstractJcrCommerceService
import com.adobe.cq.commerce.common.AbstractJcrProduct
import com.adobe.cq.commerce.common.ServiceContext
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import in.shabhushan.catalog.commerce.framework.provider.product.CatalogProductImpl
import in.shabhushan.catalog.commerce.framework.provider.session.CatalogSessionImpl
import org.apache.sling.api.SlingHttpServletRequest
import org.apache.sling.api.SlingHttpServletResponse
import org.apache.sling.api.resource.Resource

/**
 * Created by Shashi Bhushan
 *      on 13/3/17.
 */
@CompileStatic
@Slf4j
class CatalogServiceImpl extends AbstractJcrCommerceService implements CommerceService {

    private Resource resource

    protected CatalogServiceImpl(ServiceContext serviceContext, Resource resource) {
        super(serviceContext, resource)
        this.resource = resource
    }

    @Override
    public CommerceSession login(SlingHttpServletRequest slingHttpServletRequest, SlingHttpServletResponse slingHttpServletResponse) throws CommerceException {
        return new CatalogSessionImpl(this, slingHttpServletRequest, slingHttpServletResponse, resource)
    }

    /**
     * Checks if Commerce Provider Service is available or not
     *
     * @param serviceType
     *      Criteria to search for i.e. {@code serviceType} equals to {@link CommerceConstants#SERVICE_COMMERCE}
     * @return
     *      True if Commerce Provider is available according to the criteria
     */
    @Override
    boolean isAvailable(String serviceType) {
        CommerceConstants.SERVICE_COMMERCE == serviceType
    }

    @Override
    Product getProduct(String path) throws CommerceException {
        Resource resource = resolver.getResource(path)
        if (resource != null
            && CatalogProductImpl.isAProductOrVariant(resource)
            && resource.isResourceType(AbstractJcrProduct.RESOURCE_TYPE_PRODUCT)) {

            return new CatalogProductImpl(resource)
        }
    }

    @Override
    public List<String> getCountries() throws CommerceException {
        return null
    }

    @Override
    public List<String> getCreditCardTypes() throws CommerceException {
        return null
    }

    @Override
    public List<String> getOrderPredicates() throws CommerceException {
        return null
    }
}