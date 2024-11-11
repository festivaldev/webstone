interface BlockGroup {
  groupId: string;
  name: string;
  blockIds: string[];
}

interface Block {
  blockId: string;
  groupId: string | null;
  name: string;
  powered: boolean;
  power: number;
}

interface SocketConnectionData {
  hostname: string;
  port: string;
  passphrase?: string;
  useSecureSocket?: boolean;
}
