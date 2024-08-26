import { Buffer } from 'buffer';

export default class SocketClient extends EventTarget {
  private _connectionUri: URL | null = null;

  private _socket: WebSocket | null = null;
  private _readyState: number = WebSocket.CLOSED;

  public get connectionUri() {
    return this._connectionUri;
  }

  public connect(hostname: string, port: string, passphrase?: string, secure: boolean = false): void {
    this._connectionUri = new URL(`${secure ? 'wss' : 'ws'}://${hostname}:${port}`);
    this._socket = new WebSocket(
      this._connectionUri,
      passphrase ? [Buffer.from(passphrase, 'utf8').toString('hex')] : undefined,
    );

    const checkReadyState = () => {
      if (this._socket) {
        if (this._socket.readyState !== this._readyState) {
          this._readyState = this._socket.readyState;

          this.dispatchEvent(new CustomEvent('readyStateChange', { detail: this._readyState }));
        }

        requestAnimationFrame(checkReadyState);
      } else {
        console.log('SOCKET GONE');
        this._readyState = WebSocket.CLOSED;
        this.dispatchEvent(new CustomEvent('readyStateChange', { detail: this._readyState }));
      }
    };

    checkReadyState();

    this._socket?.addEventListener('open', this._onOpen.bind(this));
    this._socket?.addEventListener('close', this._onClose.bind(this));
    this._socket?.addEventListener('message', this._onMessage.bind(this));
    this._socket?.addEventListener('error', this._onError.bind(this));
  }

  public disconnect() {
    this._socket?.close();

    this._socket = null;
  }

  public send(data: any) {
    console.log('→', data);
    console.log('readyState', this._socket?.readyState, this._socket, this._connectionUri);

    if (typeof data === 'object') {
      this._socket?.send(JSON.stringify(data));
    } else {
      this._socket?.send(data);
    }
  }

  private _onOpen(e: Event): void {
    console.log('open', e);
    this._emit('open', e);
  }

  private _onClose(e: CloseEvent): void {
    // 1000: Closed by client
    // 1001: Closed by server
    // 1006: Closed abnormally, timeout
    console.log('close', e);
    this._emit('close', e);

    this._socket = null;
  }

  private _onMessage(e: MessageEvent): void {
    let { data } = e;
    try {
      data = JSON.parse(data);
    } catch {}

    console.log('←', data);

    this._emit(
      'message',
      new MessageEvent('message', {
        bubbles: e.bubbles,
        cancelable: e.cancelable,
        composed: e.composed,
        data,
        lastEventId: e.lastEventId,
        origin: e.origin,
        source: e.source,
      }),
    );
  }

  private _onError(e: Event): void {
    const event = e as ErrorEvent;

    console.log('error', e);
    this._emit('error', event);
  }

  private _emit(type: string, event: Event) {
    this.dispatchEvent(
      new (event.constructor as { new (type: string, eventInitDict?: EventInit): Event })(type, event),
    );
  }

  // #region Socket Handlers
  public setBlockState(blockId: string, powered: boolean): void {
    this.send({
      type: 'block_state',
      data: {
        blockId,
        powered,
      },
    });
  }

  public setBlockPower(blockId: string, power: number): void {
    this.send({
      type: 'block_power',
      data: {
        blockId,
        power: Math.min(Math.max(power, 0), 15),
      },
    });
  }

  public renameBlock(blockId: string, name: string): void {
    if (name?.trim().length) {
      this.send({
        type: 'rename_block',
        data: {
          blockId,
          name: name.substring(0, 64).trim(),
        },
      });
    }
  }

  public deleteBlock(blockId: string): void {
    this.send({
      type: 'unregister_block',
      data: {
        blockId,
      },
    });
  }

  public changeBlockGroup(blockId: string, groupId: string): void {
    this.send({
      type: 'change_group',
      data: {
        blockId,
        groupId,
      },
    });
  }

  public changeBlockIndex(blockId: string, newIndex: number): void {
    this.send({
      type: 'move_block',
      data: {
        blockId,
        newIndex,
      },
    });
  }

  public createGroup(name: string): void {
    if (name?.trim().length) {
      this.send({
        type: 'create_group',
        data: {
          name: name.substring(0, 64).trim(),
        },
      });
    }
  }

  public renameGroup(groupId: string, name: string): void {
    if (name?.trim().length) {
      this.send({
        type: 'rename_group',
        data: {
          groupId,
          name: name.substring(0, 64).trim(),
        },
      });
    }
  }

  public deleteGroup(groupId: string): void {
    this.send({
      type: 'delete_group',
      data: {
        groupId,
      },
    });
  }

  public changeGroupIndex(groupId: string, newIndex: number): void {
    this.send({
      type: 'move_group',
      data: {
        groupId,
        newIndex,
      },
    });
  }
  // #endregion
}
