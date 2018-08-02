@echo off

@REM Copyright (c) 2008-2018 Haulmont.
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM     http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.

set JLINK_VM_OPTIONS=
set DIR=%~dp0
"%DIR%\..\native-windows\bin\java" %JLINK_VM_OPTIONS% -m com.haulmont.cuba.cli/com.haulmont.cuba.cli.EntryPointKt %*
