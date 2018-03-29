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

import {FSMDataHandler} from "../FSMDataHandler";
import {TSFSM} from "../TSFSM";
import {DoubleClick, DoubleClickFSM} from "./DoubleClick";
import {TerminalState} from "../../../src-core/fsm/TerminalState";
import {CancellingState} from "../../../src-core/fsm/CancellingState";
import {StdState} from "../../../src-core/fsm/StdState";
import {SubFSMTransition} from "../../../src-core/fsm/SubFSMTransition";
import {InputState} from "../../../src-core/fsm/InputState";
import {FSM} from "../../../src-core/fsm/FSM";
import {OutputState} from "../../../src-core/fsm/OutputState";
import {MoveTransition} from "../MoveTransition";
import {PointInteraction} from "./PointInteraction";
import {PointInteractionData} from "./PointInteractionData";
import {EscapeKeyPressureTransition} from "../EscapeKeyPressureTransition";

export class DragLockFSM extends TSFSM<DragLockFSMHandler> {
    public readonly firstDbleClick: DoubleClickFSM;
    public readonly sndDbleClick: DoubleClickFSM;
    protected checkButton: number | undefined;

    public constructor() {
        super();
        this.firstDbleClick = new DoubleClickFSM();
        this.sndDbleClick = new DoubleClickFSM();
    }

    public buildFSM(dataHandler: DragLockFSMHandler): void {
        if (this.states.length > 1) {
            return;
        }

        super.buildFSM(dataHandler);
        const cancelDbleClick = new DoubleClickFSM();
        this.firstDbleClick.buildFSM();
        this.sndDbleClick.buildFSM();
        cancelDbleClick.buildFSM();
        const dropped = new TerminalState<Event>(this, "dropped");
        const cancelled = new CancellingState<Event>(this, "cancelled");
        const locked = new StdState<Event>(this, "locked");
        const moved = new StdState<Event>(this, "moved");

        this.addState(dropped);
        this.addState(cancelled);
        this.addState(locked);
        this.addState(moved);

        new class extends SubFSMTransition<Event> {
            private readonly _parent: DragLockFSM;

            public constructor(parent: DragLockFSM, srcState: OutputState<Event>, tgtState: InputState<Event>, fsm: FSM<Event>) {
                super(srcState, tgtState, fsm);
                this._parent = parent;
            }

            protected action(event: Event): void {
                this._parent.checkButton = this._parent.firstDbleClick.getCheckButton();
                this._parent.sndDbleClick.setCheckButton(this._parent.checkButton);
                cancelDbleClick.setCheckButton(this._parent.checkButton);
            }
        }(this, this.initState, locked, this.firstDbleClick);

        new SubFSMTransition<Event>(locked, cancelled, cancelDbleClick);

        new class extends MoveTransition {
            private readonly _parent: DragLockFSM;

            public constructor(parent: DragLockFSM, srcState: OutputState<Event>, tgtState: InputState<Event>) {
                super(srcState, tgtState);
                this._parent = parent;
            }

            public isGuardOK(event: Event): boolean {
                return super.isGuardOK(event) &&
                    (this._parent.checkButton === undefined || event instanceof MouseEvent && event.button === this._parent.checkButton);
            }

            protected action(event: Event): void {
                if (this._parent.dataHandler !== undefined && event instanceof MouseEvent) {
                    this._parent.dataHandler.onMove(event);
                }
            }
        }(this, locked, moved);

        new EscapeKeyPressureTransition(locked, cancelled);
        new EscapeKeyPressureTransition(moved, cancelled);
        new SubFSMTransition<Event>(moved, dropped, this.sndDbleClick);
    }
}

interface DragLockFSMHandler extends FSMDataHandler {
    onMove(event: MouseEvent): void;
}

export class DragLock extends PointInteraction<DragLockFSM, Event> {
    private readonly handler: DragLockFSMHandler;
    private readonly firstClick: DoubleClick;
    private readonly sndClick: DoubleClick;

    public constructor() {
        super(new DragLockFSM());

        this.handler = new class implements DragLockFSMHandler {
            private readonly _parent: DragLock;

            public constructor(parent: DragLock) {
                this._parent = parent;
            }

            public onMove(event: MouseEvent): void {
                this._parent.setPointData(event);
            }

            public reinitData(): void {
                this._parent.reinitData();
            }
        }(this);

        this.firstClick = new DoubleClick(this.getFsm().firstDbleClick);
        this.sndClick = new DoubleClick(this.getFsm().sndDbleClick);
        this.getFsm().buildFSM(this.handler);
    }

    public reinitData(): void {
        super.reinitData();
        this.firstClick.reinitData();
        this.sndClick.reinitData();
    }

    public getLockData(): PointInteractionData {
        return this.firstClick.getClickData();
    }

    public getTgtData(): PointInteractionData {
        return this.sndClick.getClickData().getButton() === undefined ? this : this.sndClick.getClickData();
    }
}