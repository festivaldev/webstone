import { ModalMotionProps } from '@/utilities/motionProps';
import {
  Button,
  Modal,
  ModalBody,
  ModalContent,
  ModalFooter,
  ModalHeader,
  Radio,
  RadioGroup,
  UseDisclosureProps,
} from '@nextui-org/react';
import React, { useMemo } from 'react';
import { useSocket } from '../SocketProvider';
import { useModalStore } from './ModalProvider';

const ChangeBlockGroupModal = ({
  groups = [],
  isOpen,
  onChange,
}: {
  groups?: BlockGroup[];
} & UseDisclosureProps): React.ReactNode => {
  const { blockId } = useModalStore();
  const { changeBlockGroup } = useSocket();

  const groupId = useMemo(
    () => groups?.find((group) => group.blockIds.includes(blockId!))?.groupId! || null,
    [blockId],
  );

  const [value, setValue] = React.useState<string>('');

  React.useEffect(() => {
    setValue(() => groupId || '');
  }, [groupId]);

  return (
    <Modal
      isDismissable={false}
      isOpen={isOpen}
      onOpenChange={() => {
        setValue(() => '');
        onChange?.(isOpen);
      }}
      placement="center"
      motionProps={ModalMotionProps}
      classNames={{
        base: 'mx-6',
      }}
    >
      <ModalContent>
        {(onClose) => (
          <>
            <ModalHeader className="flex flex-col gap-1">Change Group</ModalHeader>
            <ModalBody>
              <RadioGroup label="Select a group to assign this block to:" value={value} onValueChange={setValue}>
                <Radio value="">No group</Radio>

                {groups.map((group) => (
                  <Radio key={group.groupId} value={group.groupId}>
                    {group.name}
                  </Radio>
                ))}
              </RadioGroup>
            </ModalBody>
            <ModalFooter>
              <Button color="default" variant="flat" onPress={onClose}>
                Cancel
              </Button>
              <Button
                color="primary"
                isDisabled={value === groupId || (value === '' && groupId === null)}
                onPress={() => {
                  changeBlockGroup(blockId!, value);
                  onClose();
                }}
              >
                Confirm
              </Button>
            </ModalFooter>
          </>
        )}
      </ModalContent>
    </Modal>
  );
};

export default ChangeBlockGroupModal;
