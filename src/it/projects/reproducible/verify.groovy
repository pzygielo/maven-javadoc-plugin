/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.jar.*;

def target = new File( basedir, 'target' )
assert target.isDirectory()

def apidocs = new File( target, 'reports/apidocs' )
assert apidocs.isDirectory()

def options = new File( apidocs, 'options' )
assert options.isFile()
assert options.text.contains( "'Copyright &#169; 2010&#x2013;2020. All rights reserved.'" )

def artifact = new File( target, 'bar-0.1.0-SNAPSHOT-javadoc.jar' )
assert artifact.isFile()

// notimestamp in html files
apidocs.eachFileRecurse
{
    if ( it.name.endsWith( '.html' ) )
    {
        def line = it.text.readLines().find { it.startsWith( "<!-- Generated by javadoc" ) }
        assert line ==~ /<!-- Generated by javadoc (\(\d+\) )?-->/
    }
}

// Normalize to UTC
long normalizeUTC( String timestamp )
{
  long millis = Instant.parse( timestamp ).toEpochMilli()
  Calendar cal = Calendar.getInstance()
  cal.setTimeInMillis( millis )
  return millis - ( cal.get( Calendar.ZONE_OFFSET ) + cal.get( Calendar.DST_OFFSET ) )
}

JarFile jar = new JarFile( artifact )
assert jar.stream().count() > 0

// All entries should have the same timestamp
FileTime expectedTimestamp = FileTime.fromMillis( normalizeUTC( "2020-02-29T23:59:58Z" ) )
jar.stream().forEach { assert expectedTimestamp.equals( it.getLastModifiedTime() ) }
