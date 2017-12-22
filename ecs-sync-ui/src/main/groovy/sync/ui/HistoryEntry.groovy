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

class HistoryEntry {
    static String prefix = "archive/"
    static String idFormat = "yyyyMMdd'T'HHmmss"

    static List<HistoryEntry> list(ConfigService configService) {
        configService.listConfigObjects(prefix).collect {
            new HistoryEntry([configService: configService, xmlKey: it])
        }.sort { a, b -> b.startTime <=> a.startTime } // reverse-chronological order
    }

    ConfigService configService

    String id
    int jobId
    Date startTime
    @Lazy
    String dbName = "sync_${startTime.format(idFormat)}_${jobId}"
    @Lazy
    String xmlKey = "${prefix}${id}.xml"
    @Lazy
    String reportKey = "${prefix}report/${id}.report.csv"
    @Lazy
    String reportFileName = fileName(reportKey)
    @Lazy
    String errorsKey = "${prefix}errors/${id}.errors.csv"
    @Lazy
    String errorsFileName = fileName(errorsKey)
    @Lazy
    def allKeys = [xmlKey, reportKey, errorsKey]
    @Lazy(soft = true)
    URI reportUri = configService.configObjectQuickLink(reportKey)
    @Lazy(soft = true)
    URI errorsUri = configService.configObjectQuickLink(errorsKey)
    @Lazy(soft = true)
    SyncResult syncResult = configService.readConfigObject(xmlKey, SyncResult.class)

    def write() {
        configService.writeConfigObject(xmlKey, syncResult, 'application/xml')
    }

    boolean getReportExists() {
        return (id && configService.configObjectExists(reportKey))
    }

    boolean getErrorsExists() {
        return (id && configService.configObjectExists(errorsKey))
    }

    def setId(String id) {
        this.id = id
        this.jobId = id.tokenize('-job')?.last()?.toInteger()
        this.startTime = Date.parse(idFormat, id.tokenize('-')?.first())
    }

    def setJobId(int jobId) {
        this.jobId = jobId
        inferId()
    }

    def setStartTime(Date startTime) {
        this.startTime = startTime
        inferId()
    }

    def setXmlKey(String key) {
        setId(fileName(key).replaceFirst(/[.]xml$/, ''))
    }

    private inferId() {
        if (startTime && jobId) this.id = "${startTime.format(idFormat)}-job${jobId}"
    }

    private static String fileName(String key) {
        return key.split('/').last()
    }
}
