/*
 * Copyright 2012 Goran Ehrsson.
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
 * under the License.
 */

import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.cli.logging.GrailsConsoleAntBuilder
import grails.util.BuildSettingsHolder
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.core.io.ContextResource
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import org.codehaus.groovy.grails.web.i18n.ParamsAwareLocaleChangeInterceptor
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.codehaus.groovy.grails.web.context.GrailsConfigUtils
import grails.util.Environment
import grails.plugins.i18ndb.MessageSource
import org.apache.commons.logging.LogFactory

class I18nDbGrailsPlugin {

    private static LOG = LogFactory.getLog(MessageSource) // debug grails.plugins.i18ndb.MessageSource

    String baseDir = "grails-app/i18n"
    String watchedResources = "file:./${baseDir}/**/*.properties".toString()

    // the plugin version
    def version = "0.5"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def title = "Read i18n messages from the database"
    def author = "Goran Ehrson"
    def authorEmail = "goran@technipelago.se"
    def description = '''\
Override i18n messages in your database to allow sysadmins to modify labels, help texts, etc. in your application.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/i18n-db"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]

    // Any additional developers beyond the author specified above.
    //    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [system: "JIRA", url: "http://jira.grails.org/browse/GPI18NDB"]

    // Online location of the plugin's browseable source code.
    def scm = [url: "https://github.com/goeh/i18n-db/"]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // find i18n resource bundles and resolve basenames
        Set baseNames = []

        def messageResources
        if (application.warDeployed) {
            messageResources = parentCtx?.getResources("**/WEB-INF/${baseDir}/**/*.properties")?.toList()
        }
        else {
            messageResources = plugin.watchedResources
        }

        if (messageResources) {
            for (resource in messageResources) {
                // Extract the file path of the file's parent directory
                // that comes after "grails-app/i18n".
                String path
                if (resource instanceof ContextResource) {
                    path = StringUtils.substringAfter(resource.pathWithinContext, baseDir)
                }
                else {
                    path = StringUtils.substringAfter(resource.path, baseDir)
                }

                // look for an underscore in the file name (not the full path)
                String fileName = resource.filename
                int firstUnderscore = fileName.indexOf('_')

                if (firstUnderscore > 0) {
                    // grab everyting up to but not including
                    // the first underscore in the file name
                    int numberOfCharsToRemove = fileName.length() - firstUnderscore
                    int lastCharacterToRetain = -1 * (numberOfCharsToRemove + 1)
                    path = path[0..lastCharacterToRetain]
                }
                else {
                    // Lop off the extension - the "basenames" property in the
                    // message source cannot have entries with an extension.
                    path -= ".properties"
                }
                baseNames << "WEB-INF/" + baseDir + path
            }
        }

        messageSource(grails.plugins.i18ndb.MessageSource) { bean ->
            bean.autowire = 'byName'
            basenames = baseNames.toArray()
            fallbackToSystemLocale = false
            pluginManager = manager
            if (Environment.current.isReloadEnabled() || GrailsConfigUtils.isConfigTrue(application, GroovyPagesTemplateEngine.CONFIG_PROPERTY_GSP_ENABLE_RELOAD)) {
                def cacheSecondsSetting = application?.flatConfig?.get('grails.i18n.cache.seconds')
                if (cacheSecondsSetting != null) {
                    cacheSeconds = cacheSecondsSetting as Integer
                } else {
                    cacheSeconds = 5
                }
            }
        }

        localeChangeInterceptor(ParamsAwareLocaleChangeInterceptor) {
            paramName = "lang"
        }

        localeResolver(SessionLocaleResolver)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
        if (!applicationContext.containsBean('messageCache')) {
            LOG.warn("Please configure bean 'messageCache' to speed up i18n message lookups from database.")
        }
    }

    def onChange = { event ->
        def context = event.ctx
        if (!context) {
            log.debug("Application context not found. Can't reload")
            return
        }

        def resourcesDir = BuildSettingsHolder?.settings?.resourcesDir?.path
        if (resourcesDir) {
            String i18nDir = "${resourcesDir}/grails-app/i18n"

            def ant = new GrailsConsoleAntBuilder()

            def nativeascii = event.application.config.grails.enable.native2ascii
            nativeascii = (nativeascii instanceof Boolean) ? nativeascii : true
            if (nativeascii) {
                ant.native2ascii(src: "./grails-app/i18n",
                        dest: i18nDir,
                        includes: "*.properties",
                        encoding: "UTF-8")
            }
            else {
                ant.copy(todir: i18nDir) {
                    fileset(dir: "./grails-app/i18n", includes: "*.properties")
                }
            }
        }

        def messageSource = context.messageSource
        if (messageSource instanceof ReloadableResourceBundleMessageSource) {
            messageSource.clearCache()
        }
        else {
            log.warn "Bean messageSource is not an instance of ${ReloadableResourceBundleMessageSource.name}. Can't reload"
        }
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
