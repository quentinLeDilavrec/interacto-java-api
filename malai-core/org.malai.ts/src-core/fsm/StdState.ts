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
import {OutputStateImpl} from "./OutputStateImpl";
import {InputState} from "./InputState";
import {FSM} from "./FSM";

export class StdState<E> extends OutputStateImpl<E> implements InputState<E> {
    public constructor(stateMachine: FSM<E>, stateName: string) {
        super(stateMachine, stateName);
    }

    checkStartingState(): void {
        if (!this.getFSM().isStarted() && this.getFSM().getStartingState() === this) {
            this.getFSM().onStarting();
        }
    }

    /**
     *
     */
    public enter(): void {
        this.checkStartingState();
        this.fsm.enterStdState(this);
    }

    /**
     *
     */
    public exit(): void {
    }
}
