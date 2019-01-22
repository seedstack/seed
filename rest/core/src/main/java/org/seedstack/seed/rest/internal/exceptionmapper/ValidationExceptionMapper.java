/*
 * Copyright © 2013-2019, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.rest.internal.exceptionmapper;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.seedstack.seed.core.internal.guice.ProxyUtils;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Context
    private HttpHeaders headers;
    @Context
    private ResourceInfo resourceInfo;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        ValidationExceptionRepresentation validationErrorDetails = new ValidationExceptionRepresentation();

        ArrayList<ValidationExceptionRepresentation.ValidationError> validationErrors = new ArrayList<>();
        boolean responseError = addErrorsToList(exception, validationErrors);
        validationErrorDetails.setErrors(validationErrors);

        return Response.status(responseError ? INTERNAL_SERVER_ERROR : BAD_REQUEST)
                .entity(validationErrorDetails)
                .type(headers.getMediaType())
                .build();
    }

    private boolean addErrorsToList(ConstraintViolationException exception,
            List<ValidationExceptionRepresentation.ValidationError> validationErrors) {
        boolean responseError = false;
        Class<?> resourceClass = ProxyUtils.cleanProxy(resourceInfo.getResourceClass());

        for (ConstraintViolation<?> constraintViolation : exception.getConstraintViolations()) {
            ValidationExceptionRepresentation.ValidationError validationError =
                    new ValidationExceptionRepresentation.ValidationError();

            boolean appendToPath = false;
            StringBuilder path = new StringBuilder();
            Method method = null;
            for (Path.Node node : constraintViolation.getPropertyPath()) {
                switch (node.getKind()) {
                    case METHOD:
                        method = resolveMethod(resourceClass, node.as(Path.MethodNode.class));
                        continue;
                    case RETURN_VALUE:
                        validationError.setLocation(Location.RESPONSE_BODY.toString());
                        responseError = true;
                        appendToPath = true;
                        continue;
                    case PARAMETER:
                        ParameterInfo parameterInfo = resolveParameterInfo(
                                node.as(Path.ParameterNode.class),
                                method);
                        validationError.setLocation(parameterInfo.getLocation().toString());
                        if (!parameterInfo.isBody()) {
                            path.append(parameterInfo.getName()).append(".");
                        }
                        appendToPath = true;
                        continue;
                    default:
                        break;
                }
                if (appendToPath) {
                    path.append(node.getName()).append(".");
                }
            }

            if (path.length() > 0) {
                validationError.setPath(path.substring(0, path.length() - 1));
            }

            validationError.setMessage(constraintViolation.getMessage());

            Object invalidValue = constraintViolation.getInvalidValue();
            if (invalidValue != null) {
                validationError.setInvalidValue(String.valueOf(invalidValue));
            }

            validationErrors.add(validationError);
        }

        return responseError;
    }

    private Method resolveMethod(Class<?> resourceClass, Path.MethodNode methodNode) {
        List<Class<?>> parameterTypes = methodNode.getParameterTypes();
        try {
            return resourceClass.getMethod(
                    methodNode.getName(),
                    parameterTypes.toArray(new Class[parameterTypes.size()])
            );
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private ParameterInfo resolveParameterInfo(Path.ParameterNode node, Method method) {
        if (method != null) {
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            int parameterIndex = node.as(Path.ParameterNode.class).getParameterIndex();
            if (parameterIndex < parameterAnnotations.length) {
                for (Annotation a : parameterAnnotations[parameterIndex]) {
                    if (a instanceof QueryParam) {
                        return new ParameterInfo(Location.QUERY_PARAMETER, ((QueryParam) a).value());
                    } else if (a instanceof PathParam) {
                        return new ParameterInfo(Location.PATH_PARAMETER, ((PathParam) a).value());
                    } else if (a instanceof HeaderParam) {
                        return new ParameterInfo(Location.HEADER_PARAMETER, ((HeaderParam) a).value());
                    } else if (a instanceof CookieParam) {
                        return new ParameterInfo(Location.COOKIE_PARAMETER, ((CookieParam) a).value());
                    } else if (a instanceof FormParam) {
                        return new ParameterInfo(Location.FORM_PARAMETER, ((FormParam) a).value());
                    } else if (a instanceof MatrixParam) {
                        return new ParameterInfo(Location.MATRIX_PARAMETER, ((MatrixParam) a).value());
                    }
                }
                return new ParameterInfo(Location.REQUEST_BODY, node.getName());
            }
        }
        return new ParameterInfo(Location.UNKNOWN, node.getName());
    }

    private static class ParameterInfo {
        private final Location location;
        private final String name;

        ParameterInfo(Location location, String name) {
            this.location = location;
            this.name = name;
        }

        Location getLocation() {
            return location;
        }

        String getName() {
            return name;
        }

        boolean isBody() {
            return location == Location.REQUEST_BODY;
        }
    }

    private enum Location {
        QUERY_PARAMETER,
        PATH_PARAMETER,
        HEADER_PARAMETER,
        COOKIE_PARAMETER,
        FORM_PARAMETER,
        MATRIX_PARAMETER,
        REQUEST_BODY,
        RESPONSE_BODY,
        UNKNOWN
    }
}
