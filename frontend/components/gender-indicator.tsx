"use client"

import { cn } from "@/lib/utils"

type Gender = 'male' | 'female' | 'diverse'

interface GenderIndicatorProps {
  gender: Gender
  size?: 'sm' | 'md'
  className?: string
}

// Subtle geometric shapes in neutral stone color - no gendered color associations
export function GenderIndicator({ gender, size = 'sm', className }: GenderIndicatorProps) {
  const sizeClasses = size === 'sm' ? "w-2.5 h-2.5" : "w-3 h-3"
  
  if (gender === 'male') {
    // Square shape
    return (
      <span
        className={cn(
          "shrink-0 border border-stone-400/60",
          sizeClasses,
          className
        )}
        title="Male"
      />
    )
  }
  
  if (gender === 'female') {
    // Circle shape
    return (
      <span
        className={cn(
          "shrink-0 rounded-full border border-stone-400/60",
          sizeClasses,
          className
        )}
        title="Female"
      />
    )
  }
  
  // Diverse - diamond shape (rotated square)
  return (
    <span
      className={cn(
        "shrink-0 border border-stone-400/60 rotate-45",
        size === 'sm' ? "w-2 h-2" : "w-2.5 h-2.5",
        className
      )}
      title="Diverse"
    />
  )
}
