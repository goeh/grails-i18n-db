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

import org.springframework.dao.DataIntegrityViolationException
import net.sf.ehcache.Cache

class AppMessageController {

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    static navigation = [
            [group: 'admin',
                    order: 930,
                    title: 'appMessage.label',
                    action: 'list'
            ],
            [group: 'appMessage',
                    order: 10,
                    title: 'appMessage.create.label',
                    action: 'create',
                    isVisible: { actionName != 'create' }
            ],
            [group: 'appMessage',
                    order: 20,
                    title: 'appMessage.list.label',
                    action: 'list',
                    isVisible: { actionName != 'list' }
            ]
    ]

    def grailsApplication
    def appMessageService

    def index() {
        redirect action: 'list', params: params
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 20, 100)
        [appMessageList: AppMessage.list(params), appMessageTotal: AppMessage.count()]
    }

    def create() {
        switch (request.method) {
            case 'GET':
                [appMessage: new AppMessage(params)]
                break
            case 'POST':
                def appMessage = new AppMessage(params)
                if (!appMessage.save(flush: true)) {
                    render view: 'create', model: [appMessage: appMessage]
                    return
                }

                flash.message = message(code: 'default.created.message', args: [message(code: 'appMessage.label', default: 'AppMessage'), appMessage.toString()])
                redirect action: 'list'
                break
        }
    }

    def edit() {
        switch (request.method) {
            case 'GET':
                def appMessage = AppMessage.get(params.id)
                if (!appMessage) {
                    flash.message = message(code: 'default.not.found.message', args: [message(code: 'appMessage.label', default: 'AppMessage'), params.id])
                    redirect action: 'list'
                    return
                }

                [appMessage: appMessage]
                break
            case 'POST':
                def appMessage = AppMessage.get(params.id)
                if (!appMessage) {
                    flash.message = message(code: 'default.not.found.message', args: [message(code: 'appMessage.label', default: 'AppMessage'), params.id])
                    redirect action: 'list'
                    return
                }

                if (params.version) {
                    def version = params.version.toLong()
                    if (appMessage.version > version) {
                        appMessage.errors.rejectValue('version', 'default.optimistic.locking.failure',
                                [message(code: 'appMessage.label', default: 'AppMessage')] as Object[],
                                "Another user has updated this AppMessage while you were editing")
                        render view: 'edit', model: [appMessage: appMessage]
                        return
                    }
                }

                appMessage.properties = params

                if (!appMessage.save(flush: true)) {
                    render view: 'edit', model: [appMessage: appMessage]
                    return
                }

                appMessageService.removeFromCache(appMessage)

                flash.message = message(code: 'default.updated.message', args: [message(code: 'appMessage.label', default: 'AppMessage'), appMessage.toString()])
                redirect action: 'list'
                break
        }
    }

    def delete() {
        def appMessage = AppMessage.get(params.id)
        if (!appMessage) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'appMessage.label', default: 'AppMessage'), params.id])
            redirect action: 'list'
            return
        }

        try {
            def tombstone = appMessage.toString()
            appMessage.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'appMessage.label', default: 'AppMessage'), tombstone])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'appMessage.label', default: 'AppMessage'), params.id])
            redirect action: 'edit', id: params.id
        }
    }

    def export() {
        def zipFile = appMessageService.export()
        if (zipFile?.exists()) {
            try {
                def filename = grailsApplication.metadata['app.name'] ?: 'i18n'
                response.setHeader("Content-disposition", "attachment; filename=${filename}.zip")
                response.contentType = "application/zip"
                response.characterEncoding = "UTF-8"
                response.setContentLength(zipFile.length().intValue())
                response.setHeader("Pragma", "")
                response.setHeader("Cache-Control", "private,no-store,max-age=120")
                Calendar cal = Calendar.getInstance()
                cal.add(Calendar.MINUTE, 2)
                response.setDateHeader("Expires", cal.getTimeInMillis())

                def out = response.outputStream
                zipFile.withInputStream {is ->
                    out << is
                }
                out.flush()
            } finally {
                zipFile.delete()
            }
        } else {
            redirect action: 'list'
        }
    }
}
