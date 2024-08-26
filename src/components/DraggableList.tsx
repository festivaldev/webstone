import { Bars3Icon } from '@heroicons/react/24/outline';
import React from 'react';

const DraggableList = ({
  items,
  template,
  onChange,
}: {
  items: any[];
  template: (item: any, index: number) => React.ReactNode;
  onChange?: (items: any[], item: any, index: number) => void;
}): React.ReactNode => {
  const [draggedItem, setDraggedItem] = React.useState<any>();
  const [itemList, setItemList] = React.useState<any[]>([]);

  const onDragStart = (e: React.DragEvent<HTMLElement>, index: number) => {
    const target = e.target as HTMLElement;
    const parent = target.parentElement!.parentElement!;

    setDraggedItem(() => itemList[index]);
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('text/plain', '');

    if (/firefox/i.test(navigator.userAgent)) {
      e.dataTransfer.setDragImage(parent, 8, 8);
    } else {
      e.dataTransfer.setDragImage(
        parent,
        target.getBoundingClientRect().width / 2,
        target.getBoundingClientRect().height / 2,
      );
    }
  };

  const onDragOver = (e: React.DragEvent<HTMLElement>, index: number) => {
    e.preventDefault();

    const draggedOverItem = itemList[index];

    if (draggedItem === draggedOverItem) {
      return;
    }

    const _items = itemList.filter((item) => item !== draggedItem);
    _items.splice(index, 0, draggedItem);

    setItemList(() => _items);
  };

  const onDrop = () => {
    onChange?.(itemList, draggedItem, itemList.indexOf(draggedItem));
    setDraggedItem(() => undefined);
  };

  React.useEffect(() => {
    setItemList(() => [...items]);
  }, [items]);

  return (
    <ul className="rounded-lg border-1 border-default-200">
      {itemList &&
        itemList.map((item, index) => (
          <li
            key={index}
            className="border-default-200 px-4 py-3 [&:not(:last-child)]:border-b-1"
            onDragOver={(e) => onDragOver(e, index)}
          >
            <div>
              <div className="flex items-center">
                <div
                  className="mr-3 text-white/40"
                  draggable
                  onDragStart={(e) => onDragStart(e, index)}
                  onDrop={onDrop}
                >
                  <Bars3Icon className="size-5 cursor-grab active:cursor-grabbing" />
                </div>

                {template(item, index)}
              </div>
            </div>
          </li>
        ))}
    </ul>
  );
};

export default DraggableList;
