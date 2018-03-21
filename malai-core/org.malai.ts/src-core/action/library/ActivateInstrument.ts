/*
 * This file is part of Malai.
 * Copyright (c) 2009-2018 Arnaud BLOUIN
 * Malai is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 * Malai is distributed without any warranty; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 */

import {InstrumentAction} from "./InstrumentAction";
import {Instrument} from "../../instrument/Instrument";

/**
 * This action activates an instrument.
 * @author Arnaud Blouin
 * @param {*} instrument
 * @class
 * @extends InstrumentAction
 */
export class ActivateInstrument extends InstrumentAction {
    public constructor(instrument?: Instrument<any>) {
        super(instrument);
    }

    protected doActionBody(): void {
        if (this.instrument) {
            this.instrument.setActivated(true);
        }
    }
}
