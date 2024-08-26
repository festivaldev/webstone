import { type HTMLMotionProps } from 'framer-motion';

export const ModalMotionProps: HTMLMotionProps<'section'> = {
  initial: {
    opacity: 0,
    scale: 1.1,
    y: 0,
  },
  variants: {
    enter: {
      opacity: 1,
      scale: 1,
      y: 0,
      transition: {
        duration: 0.4,
        ease: [0.25, 1, 0.5, 1],
      },
    },
    exit: {
      opacity: 0,
      scale: 0.9,
      transition: {
        duration: 0.25,
        ease: [0.5, 1, 0.89, 1],
      },
    },
  },
};
