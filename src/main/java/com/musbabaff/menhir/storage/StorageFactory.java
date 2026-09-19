/*
 * Copyright (c) 2026 musbabaff. Licensed under MPL-2.0.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.musbabaff.menhir.storage;

import com.musbabaff.menhir.config.storage.StorageSettings;
import com.musbabaff.menhir.storage.mysql.MySqlStorage;
import com.musbabaff.menhir.storage.yaml.YamlStorage;

import java.io.File;
import java.util.logging.Logger;

/**
 * Creates the configured {@link StorageProvider}.
 */
public final class StorageFactory {

    private StorageFactory() {
    }

    public static StorageProvider create(StorageSettings.Type type, StorageSettings settings, File storageFolder, Logger logger) {
        return switch (type) {
            case YAML -> new YamlStorage(storageFolder, logger);
            case MYSQL -> new MySqlStorage(settings, storageFolder, logger);
        };
    }

    public static StorageProvider create(StorageSettings settings, File storageFolder, Logger logger) {
        return create(settings.getType(), settings, storageFolder, logger);
    }

}
