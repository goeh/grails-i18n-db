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

package grails.plugins.i18ndb

import java.text.MessageFormat
import net.sf.ehcache.Ehcache
import net.sf.ehcache.Element
import org.codehaus.groovy.grails.context.support.PluginAwareResourceBundleMessageSource
import org.apache.commons.logging.LogFactory

/**
 *
 * @author Goran Ehrsson
 * @since 0.1
 */
class MessageSource extends PluginAwareResourceBundleMessageSource {

    private static LOG = LogFactory.getLog(MessageSource)

    Ehcache messageCache

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        def key = new MessageKey(code, locale)
        def format = messageCache?.get(key)?.value
        if (format == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug('cache MISS ' + code)
            }
            format = findCode(code, locale) {a, l ->
                super.resolveCode(a, l)
            }
            if (format != null && messageCache != null) {
                messageCache.put(new Element(key, format))
            }
        } else if (!(format instanceof MessageFormat)) {
            format = new MessageFormat(format.toString(), locale)
            if (LOG.isDebugEnabled()) {
                LOG.debug('cache HIT ' + code)
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug('cache HIT ' + code)
        }
        return format
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        def key = new MessageKey(code, locale)
        def format = messageCache?.get(key)?.value
        if (format == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug('- ' + code)
            }
            format = findCode(code, locale) {a, l ->
                super.resolveCodeWithoutArguments(a, l)
            }
            if (format != null && messageCache != null) {
                messageCache.put(new Element(key, format))
            }
        } else if (LOG.isDebugEnabled()) {
            LOG.debug('+ ' + code)
        }
        return (format instanceof MessageFormat) ? format.toPattern() : format
    }

    private Object findCode(String code, Locale locale, Closure fallback) {
        // Try with exact match
        def alternatives = [code]
        // Add '.label' suffix.
        if (!code.endsWith('.label')) {
            alternatives << (code + '.label')
        }
        // Replace first group with 'default'
        def altCode = (code =~ /^[^\.]+\./).replaceFirst("default.")
        if (altCode != code) {
            alternatives << altCode
            if (!altCode.endsWith('.label')) {
                alternatives << (altCode + '.label')
            }
        }

        def result = AppMessage.withCriteria {
            inList('code', alternatives)
            or {
                eq('locale', locale.toString()) // sv_SE, en_UK
                eq('locale', locale.language) // sv, en
                isNull('locale')
            }
            order('locale', 'desc')
        }

        // If multiple result, make sure we return the most wanted message.
        def format
        for (alt in alternatives) {
            def msg = result.find {it.code == alt}
            format = msg ? new MessageFormat(msg.text, locale) : fallback(alt, locale)
            if (format != null) {
                break
            }
        }
        return format
    }
}
