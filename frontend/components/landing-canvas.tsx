"use client"

import { motion } from "framer-motion"
import Image from "next/image"

interface LandingCanvasProps {
  onNavigate: (section: 'tree' | 'map' | 'archive') => void
}

const sections = [
  {
    id: 'tree' as const,
    title: 'Family Tree',
    description: 'Explore your lineage',
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/ChatGPT%20Image%20Apr%2011%2C%202026%2C%2009_43_00%20AM-wrkQd2fhLDDovGvev3GEZHcwdvjpc1.png',
  },
  {
    id: 'map' as const,
    title: 'Time Map',
    description: 'Journey through history',
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/ChatGPT%20Image%20Apr%2011%2C%202026%2C%2010_13_42%20AM-jA3q5G5RjTNXOIu2dybtvcV3AAXANE.png',
  },
  {
    id: 'archive' as const,
    title: 'Archive',
    description: 'Document memories',
    image: 'https://hebbkx1anhila5yf.public.blob.vercel-storage.com/ChatGPT%20Image%20Apr%2011%2C%202026%2C%2009_43_30%20AM-FiSVugbwACvTShp5oRsMrT5r0sAbOG.png',
  },
]

export function LandingCanvas({ onNavigate }: LandingCanvasProps) {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center px-8 py-16">
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.8, ease: [0.22, 1, 0.36, 1] }}
        className="text-center mb-20"
      >
        <h1 className="font-serif text-4xl md:text-6xl lg:text-7xl font-bold tracking-tight text-foreground mb-4 text-balance">
          The Living Archive
        </h1>
        <p className="text-muted-foreground text-lg md:text-xl max-w-lg mx-auto font-normal italic">
          Every branch tells a story.
        </p>
      </motion.div>

      <div className="flex flex-col md:flex-row items-center justify-center gap-16 md:gap-20 lg:gap-28 max-w-6xl mx-auto">
        {sections.map((section, index) => (
          <motion.button
            key={section.id}
            onClick={() => onNavigate(section.id)}
            initial={{ opacity: 0, y: 40 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{
              duration: 0.7,
              delay: 0.2 + index * 0.15,
              ease: [0.22, 1, 0.36, 1],
            }}
            className="group relative cursor-pointer focus:outline-none flex flex-col items-center"
          >
            <motion.div 
              className="relative w-48 h-56 md:w-56 md:h-64 lg:w-64 lg:h-72 mb-6"
              whileHover={{ 
                scale: 1.12,
                transition: { duration: 0.4, ease: [0.22, 1, 0.36, 1] }
              }}
              whileTap={{ scale: 1.05 }}
            >
              <Image
                src={section.image}
                alt={section.title}
                fill
                className="object-contain drop-shadow-lg transition-all duration-400"
                sizes="(max-width: 768px) 192px, (max-width: 1024px) 224px, 256px"
              />
            </motion.div>
            
            <div className="text-center">
              <h2 className="font-serif text-xl md:text-2xl font-semibold text-foreground mb-1 group-hover:text-terracotta transition-colors duration-300">
                {section.title}
              </h2>
              <p className="text-muted-foreground text-sm font-normal">
                {section.description}
              </p>
            </div>
          </motion.button>
        ))}
      </div>

      <motion.p
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 1.2, duration: 0.8 }}
        className="mt-20 text-muted-foreground text-xs font-normal uppercase tracking-[0.2em]"
      >
        Select to begin
      </motion.p>
    </div>
  )
}
