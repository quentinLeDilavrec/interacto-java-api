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

import {Transition} from "./Transition";
import {OutputState} from "./OutputState";
import {InputState} from "./InputState";
import {Optional} from "../../src/util/Optional";

export class TimeoutTransition<E> extends Transition<E> {
    /**
     * The timeoutDuration in ms.
     */
    private timeoutDuration: () => number;

    /**
     * The current thread in progress.
     */
    private timeoutThread: any; //java.lang.Thread;

    private timeouted: boolean;

    public constructor(srcState: OutputState<E>, tgtState: InputState<E>, timeout: () => number) {
        super(srcState, tgtState);
        this.timeouted = false;
        this.timeoutDuration = timeout;
        this.timeouted = false;
    }

    /**
     * Launches the timer.
     */
    public startTimeout(): void {
        if (this.timeoutThread === undefined) {
            //     this.timeoutThread = new java.lang.Thread(() => {
            let time = this.timeoutDuration();
            if (time > 0) {
                //             try {
                //                 java.lang.Thread.sleep(time);
                this.timeouted = true;
                this.src.getFSM().onTimeout();
                //             } catch(ex) {
                //             };
            }
            //     this.timeoutThread.start();
        }
    }

    /**
     * Stops the timer.
     */
    public stopTimeout(): void {
        if (this.timeoutThread !== undefined) {
            //     this.timeoutThread.interrupt();
            this.timeoutThread = undefined;
        }
    }

    /**
     *
     * @param {*} event
     * @return {boolean}
     */
    accept(event: E | undefined): boolean {
        return this.timeouted;
    }

    /**
     *
     * @param {*} event
     * @return {boolean}
     */
    isGuardOK(event: E | undefined): boolean {
        return this.timeouted;
    }

    public execute(event?: E): Optional<InputState<E>> {
        try {
            if (this.accept(event) && this.isGuardOK(event)) {
                this.src.exit();
                this.action(event);
                this.tgt.enter();
                this.timeouted = false;
                return Optional.of(this.tgt);
            }
            return Optional.empty<InputState<E>>();
        } catch (ex) {
            this.timeouted = false;
            throw ex;
        }
    }

    /**
     *
     * @return {*[]}
     */
    public getAcceptedEvents(): Set<String> {
        return new Set();
    }
}
