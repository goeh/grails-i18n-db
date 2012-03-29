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

import java.util.zip.ZipInputStream


class AppMessageServiceTests extends GroovyTestCase {

    def grailsApplication
    def appMessageService

    void testExportNothing() {
        assert appMessageService.export() == null
    }

    void testExportDefaultLocale() {
        // Add some messages
        new AppMessage(code: 'default.list.label', text: 'List all {0}').save()
        new AppMessage(code: 'default.create.label', text: 'Create a shiny new {0}').save()
        new AppMessage(code: 'default.edit.label', text: 'Edit this {0}').save()

        // Export.
        def file = appMessageService.export()
        assert file != null
        assert file.exists()

        def zis = new ZipInputStream(file.newInputStream())
        def entry = zis.getNextEntry()
        assert entry != null
        def appName = grailsApplication.metadata['app.name']
        assert entry.name == (appName + '.properties')

        // The zip archive should only contain one language/file
        assert zis.getNextEntry() == null

        zis.close()
        file.delete()
    }

    void testExportWithSpecifiedName() {
        // Add some messages
        new AppMessage(code: 'default.list.label', text: 'List all {0}').save()
        new AppMessage(code: 'default.create.label', text: 'Create a shiny new {0}').save()
        new AppMessage(code: 'default.edit.label', text: 'Edit this {0}').save()

        // Export.
        def file = appMessageService.export('test')
        assert file != null
        assert file.exists()

        def zis = new ZipInputStream(file.newInputStream())
        def entry = zis.getNextEntry()
        assert entry != null
        assert entry.name == 'test.properties'

        // The zip archive should only contain one language/file
        assert zis.getNextEntry() == null

        zis.close()
        file.delete()
    }

    void testExportTwoLanguages() {
        // Add some french messages
        new AppMessage(locale: 'fr', code: 'default.paginate.prev', text: 'Précédent').save()
        new AppMessage(locale: 'fr', code: 'default.paginate.next', text: 'Suivant').save()

        // Add some swedish messages.
        new AppMessage(locale: 'sv', code: 'default.paginate.prev', text: 'Föregående').save()
        new AppMessage(locale: 'sv', code: 'default.paginate.next', text: 'Nästa').save()

        // Export.
        def file = appMessageService.export('test')
        assert file != null
        assert file.exists()

        def zis = new ZipInputStream(file.newInputStream())
        def entry
        2.times {
            entry = zis.getNextEntry()
            assert entry != null
            // The order is random so we don't know what language comes first.
            assert entry.name == 'test_fr.properties' || entry.name == 'test_sv.properties'
        }

        // The zip archive should only contain two languages/files
        assert zis.getNextEntry() == null

        zis.close()
        file.delete()
    }
}
