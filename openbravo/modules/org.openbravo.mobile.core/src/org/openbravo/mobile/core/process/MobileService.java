/*
 ************************************************************************************
 * Copyright (C) 2012-2016 Openbravo S.L.U.
 * Licensed under the Openbravo Commercial License version 1.0
 * You may obtain a copy of the License at http://www.openbravo.com/legal/obcl.html
 * or in the legal folder of this module distribution.
 ************************************************************************************
 */
package org.openbravo.mobile.core.process;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.weld.WeldUtils;
import org.openbravo.service.json.JsonUtils;
import org.openbravo.service.web.InvalidRequestException;
import org.openbravo.service.web.WebServiceUtil;

/**
 * A web service which provides Mobile services.
 * 
 * @author adrianromero
 */
public class MobileService extends WebServiceAuthenticatedServlet {

  private static final Logger log = Logger.getLogger(MobileService.class);
  private static final long serialVersionUID = 1L;

  private static String SERVLET_PATH = "org.openbravo.mobile.core.service.jsonrest";

  private boolean bypassImportEntry = false;

  public void init(ServletConfig config) {
    super.init(config);
    bypassImportEntry = OBPropertiesProvider.getInstance().getBooleanProperty(
        "import.bypass.entry.logic");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    doGetOrPost(request, response, null);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    doGetOrPost(request, response, getRequestContent(request));
  }

  private void doGetOrPost(HttpServletRequest request, HttpServletResponse response, String content)
      throws IOException, ServletException {

    String[] pathparts = checkSetParameters(request, response);
    if (pathparts == null) {
      return;
    }
    try {
      final Object jsonsent = getContentAsJSON((content == null && pathparts.length == 3) ? java.net.URLDecoder
          .decode(pathparts[2], "UTF-8") : content);

      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");

      if (pathparts.length == 1 || "hql".equals(pathparts[1])) {
        final Writer w = response.getWriter();
        execThingArray(w, jsonsent);
        w.close();
      } else {
        // Command it is a class name
        try {
          if (jsonsent instanceof JSONObject) {
            // first write to a stringwriter
            // if an exception occurs then the exception can be written to a fresh response
            // writer
            final Writer w = new StringWriter();
            w.write("{\"response\":{");
            execServiceName(w, pathparts[1], (JSONObject) jsonsent);
            w.write("}}");
            w.close();

            // got here so probably fine to write to the response now
            final Writer respWriter = response.getWriter();
            respWriter.write(w.toString());
            respWriter.close();

          } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
                "Content is not a JSON object.")));
          }
        } catch (ClassNotFoundException e) {
          log.error(e.getMessage(), e);
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
              "Command class not found: " + pathparts[1])));
        }
      }
    } catch (Throwable t) {
      log.error(t.getMessage(), t);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      writeResult(response, JsonUtils.convertExceptionToJson(t));
    }
  }

  private void execThingArray(Writer w, Object jsonContent) throws Exception {

    if (jsonContent instanceof JSONArray) {
      JSONArray jsonArray = (JSONArray) jsonContent;
      w.write("{\"response\":{");
      try {
        w.write("\"responses\":[");
        for (int i = 0; i < jsonArray.length(); i++) {
          if (i > 0) {
            w.write(',');
          }
          w.write('{');
          execThing(w, jsonArray.getJSONObject(i));
          w.write('}');
        }
        w.write("]");
      } catch (JSONException e) {
        w.write("],");
        JSONRowConverter.addJSONExceptionFields(w, e);
      }
      w.write("}}");
    } else if (jsonContent instanceof JSONObject) {
      w.write("{\"response\":{");
      execThing(w, (JSONObject) jsonContent);
      w.write("}}");
    } else {
      w.write(JsonUtils.convertExceptionToJson(new JSONException("Expected JSON object or array.")));
    }
  }

  private void execThing(Writer w, JSONObject jsonsent) throws Exception {

    // this is the response object
    // JSONProcess writes the properties of the response object.

    if (jsonsent.has("className")) {
      try {
        execServiceName(w, jsonsent.getString("className"), jsonsent);
      } catch (JSONException e) {
        JSONRowConverter.addJSONExceptionFields(w, e);
      } catch (ClassNotFoundException e) {
        JSONRowConverter.addJSONExceptionFields(w, e);
      }
    } else if (jsonsent.has("process")) { // It is a Process
      execProcedureName(w, jsonsent);
    } else {
      JSONRowConverter.addJSONExceptionFields(w, new JSONException(
          "Expected one of the following properties: \"className\" or \"process\"."));
    }
  }

  /**
   * Executes the JSONProcess implementing the serviceName. The JSONProcesses are sorted by priority
   * and each one can define if it is needed to call the next JSONProcess or not. The response
   * writer is filled with the result of the last executed JSONProcess. Each JSONProcess can
   * determine if the next class has to be executed.
   * 
   * @param w
   *          Writer where the result is stored.
   * @param serviceName
   *          the name of the Mobile Service to execute.
   * @param jsonsent
   *          JSONObject with the Mobile Service input parameters.
   * 
   */
  private void execServiceName(Writer w, String serviceName, JSONObject jsonsent) throws Exception {
    List<? extends JSONProcess> processes = getServiceClassInstances(serviceName);
    // Create a new writer for each process. Only write on the given writer the last execution
    // result.
    Writer tmpwriter = null;
    int i = 0;
    for (JSONProcess process : processes) {
      i++;
      tmpwriter = new StringWriter();
      try {
        execProcess(tmpwriter, process, jsonsent);
      } catch (Exception e) {
        if (processes.size() == i
            || (process instanceof SecuredJSONProcess && !((SecuredJSONProcess) process)
                .executeNextServiceClassOnFailure())) {
          tmpwriter.close();
          throw e;
        }
      }
      tmpwriter.close();
      if (!(process instanceof SecuredJSONProcess && ((SecuredJSONProcess) process)
          .executeNextServiceClass())) {
        break;
      }
    }
    if (tmpwriter != null) {
      w.write(tmpwriter.toString());
    }
  }

  /**
   * Executes a JSONProcess with the data sent in the JSONObject.
   * 
   * @param w
   *          Writer where the result is stored.
   * @param process
   *          JSONProcess to execute.
   * @param jsonsent
   *          JSONObject with the process input parameters.
   */
  private void execProcess(Writer w, JSONProcess process, JSONObject jsonsent) throws IOException,
      ServletException {

    if (process instanceof SecuredJSONProcess) {
      if (!bypassImportEntry && process instanceof DataSynchronizationImportProcess) {
        ((DataSynchronizationImportProcess) process).executeCreateImportEntry(w, jsonsent);
      } else {
        ((SecuredJSONProcess) process).secureExec(w, jsonsent);
      }
    } else {
      log.warn("Executing unsecure process " + process.getClass());
      if (process instanceof DataSynchronizationImportProcess) {
        ((DataSynchronizationImportProcess) process).executeCreateImportEntry(w, jsonsent);
      } else {
        process.exec(w, jsonsent);
      }
    }
  }

  /**
   * Builds a sorted List of instances that implement the serviceName. The implementation is done
   * using the MobileServiceClassSelector. To ensure backwards compatibility in case that exists a
   * class with the same name than the service name and that it does not implement the
   * MobileServiceClassSelector it is also added to the List.
   * 
   * @param serviceName
   *          The Mobile Service name that is going to be executed.
   * @return The sorted list of classes implementing the Mobile Service.
   * @throws ClassNotFoundException
   *           Exception thrown when no class implementing the service name is found.
   */
  private List<? extends JSONProcess> getServiceClassInstances(String serviceName)
      throws ClassNotFoundException {
    BeanManager bm = WeldUtils.getStaticInstanceBeanManager();
    List<JSONProcess> processes = new ArrayList<JSONProcess>();
    boolean isDefaultClassMissing = true;
    Set<Bean<?>> beans = bm.getBeans(JSONProcess.class, WeldUtils.ANY_LITERAL,
        new MobileServiceClassSelector(serviceName));
    for (Bean<?> bean : beans) {
      processes.add((JSONProcess) bm.getReference(bean, JSONProcess.class,
          bm.createCreationalContext(bean)));
      if (bean.getBeanClass().getName().equals(serviceName)) {
        isDefaultClassMissing = false;
      }
    }
    // If no bean with classname like the service name is added try to add to the list. This is
    // added for backwards compatibility to include service classes that are not implementing the
    // qualifier.
    try {
      if (isDefaultClassMissing) {
        @SuppressWarnings("unchecked")
        Class<JSONProcess> process = (Class<JSONProcess>) Class.forName(serviceName);
        JSONProcess proc = WeldUtils.getInstanceFromStaticBeanManager(process);

        processes.add(proc);
      }
    } catch (ClassNotFoundException ignore) {
      // Only throw ClassNotFoundException if no JSONProcess is found implementing the service name.
    }

    if (processes.isEmpty()) {
      throw new ClassNotFoundException();
    }
    Collections.sort(processes, new ServiceProcessComparator());

    return processes;
  }

  /**
   * Executes the Procedure set in the jsonsent JSONObject.
   * 
   * @param w
   *          Writer where the result is stored.
   * @param jsonsent
   *          JSONObject with the process input parameters.
   * @throws IOException
   *           Exception thrown on errors writing the response.
   * @throws ServletException
   *           Exception thrown by the JSONProcess execution.
   */
  private void execProcedureName(Writer w, JSONObject jsonsent) throws IOException,
      ServletException {

    ProcessProcedure proc = WeldUtils.getInstanceFromStaticBeanManager(ProcessProcedure.class);
    proc.secureExec(w, jsonsent);
  }

  private String[] checkSetParameters(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if (!request.getRequestURI().contains("/" + SERVLET_PATH)) {
      writeResult(response, JsonUtils.convertExceptionToJson(new InvalidRequestException(
          "Invalid url, the path should contain the service name: " + SERVLET_PATH)));
      return null;
    }

    final int nameIndex = request.getRequestURI().indexOf(SERVLET_PATH);
    final String servicePart = request.getRequestURI().substring(nameIndex);
    final String[] pathParts = WebServiceUtil.getInstance().getSegments(servicePart);
    if (pathParts.length == 0 || !pathParts[0].equals(SERVLET_PATH)) {
      writeResult(
          response,
          JsonUtils.convertExceptionToJson(new InvalidRequestException("Invalid url: "
              + request.getRequestURI())));
      return null;
    }

    return pathParts;
  }

  private static class ServiceProcessComparator implements Comparator<JSONProcess> {
    private int getProcessPriority(JSONProcess proc) {
      if (proc instanceof SecuredJSONProcess) {
        return ((SecuredJSONProcess) proc).getPriority();
      }
      return 100;
    }

    @Override
    public int compare(JSONProcess proc1, JSONProcess proc2) {
      return getProcessPriority(proc1) - getProcessPriority(proc2);
    }

  }

  @javax.inject.Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ ElementType.TYPE })
  public @interface MobileServiceQualifier {
    String serviceName();
  }

  @SuppressWarnings("all")
  public static class MobileServiceClassSelector extends AnnotationLiteral<MobileServiceQualifier>
      implements MobileServiceQualifier {
    private static final long serialVersionUID = 1L;

    final String serviceName;

    public MobileServiceClassSelector(String entity) {
      this.serviceName = entity;
    }

    public String serviceName() {
      return serviceName;
    }
  }
}
