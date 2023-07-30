<?xml version="1.0" encoding="UTF-8"?>
<request id="${body}">
    <appId>APP2</appId>
    <attribute>FARM</attribute>
    <flocks>
        <flock>
            <location>Grange</location>
            <total>${headers.RANDOM_COUNT}</total>
            <type>APP2 - Chicken</type>
        </flock>
    </flocks>
    <loans>
        <loan>
            <status>approved</status>
            <value>${headers.RANDOM_AMOUNT}</value>
        </loan>
    </loans>
</request>