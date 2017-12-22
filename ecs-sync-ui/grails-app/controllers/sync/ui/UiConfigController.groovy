/*
 * Copyright 2013-2017 EMC Corporation. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package sync.ui

import grails.transaction.Transactional
import grails.util.GrailsUtil

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.OK

@Transactional(readOnly = true)
class UiConfigController implements ConfigAccessor {
    static allowedMethods = [save: 'POST', update: 'PUT']

    def scheduleService

    def index() {
        def uiConfig = UiConfig.first() ?: new UiConfig()
        if (request.exception) uiConfig.errors.reject("", GrailsUtil.extractRootCause(request.exception).getMessage())
        if (uiConfig.id) respond uiConfig, view: 'edit'
        else respond uiConfig, view: 'create'
    }

    def create() {
        def uiConfig = new UiConfig(params)
        respond uiConfig
    }

    @Transactional
    def save(UiConfig uiConfig) {
        if (params.readConfig) getConfigService(uiConfig).readConfig(uiConfig)

        if (uiConfig.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond uiConfig.errors, view: 'create'
            return
        }

        uiConfig.save flush: true

        configService.writeConfig(uiConfig)

        scheduleService.scheduleAllJobs()

        request.withFormat {
            form multipartForm {
                flash.message = 'uiConfig saved'
                redirect action: 'edit', id: uiConfig.id, method: 'GET'
            }
            '*' { respond uiConfig, [status: CREATED] }
        }
    }

    def edit(UiConfig uiConfig) {
        if (uiConfig == null) redirect action: 'create'
        else respond uiConfig
    }

    @Transactional
    def update(UiConfig uiConfig) {
        if (params.readConfig) getConfigService(uiConfig).readConfig(uiConfig)

        if (uiConfig.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond uiConfig.errors, view: 'edit'
            return
        }

        uiConfig.save flush: true

        configService.writeConfig(uiConfig)

        scheduleService.scheduleAllJobs()

        request.withFormat {
            form multipartForm {
                flash.message = 'uiConfig saved'
                redirect action: 'edit', id: uiConfig.id, method: 'GET'
            }
            '*' { respond uiConfig, [status: OK] }
        }
    }
}
